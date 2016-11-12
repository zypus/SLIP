package com.zypus.Maze.view

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.zypus.Maze.algorithms.GenericMazeEvolution
import com.zypus.Maze.algorithms.RnnMazeEvolution
import com.zypus.Maze.controller.ARobotController
import com.zypus.Maze.controller.RnnRobotController
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.Robot
import com.zypus.SLIP.algorithms.genetic.Entity
import com.zypus.SLIP.algorithms.genetic.EvolutionState
import com.zypus.SLIP.controllers.StatisticDelegate
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.Statistic
import com.zypus.utilities.LineSegment
import com.zypus.utilities.deg
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import mikera.vectorz.Vector2
import tornadofx.*
import java.io.File

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/10/2016
 */
class SimpleMazeView : View() {
	override val root = hbox {

		padding = Insets(5.0)

		val walls = arrayListOf(
				LineSegment(Vector2(0.0, 0.0), Vector2(0.0, 500.0)),
				LineSegment(Vector2(0.0, 500.0), Vector2(500.0, 500.0)),
				LineSegment(Vector2(0.0, 0.0), Vector2(500.0, 0.0)),
				LineSegment(Vector2(500.0, 0.0), Vector2(500.0, 500.0)),
				LineSegment(Vector2(100.0, 400.0), Vector2(400.0, 100.0))
		)

		val start = Vector2(100.0, 100.0)
		val goal = Vector2(400.0, 400.0)

		val maze = Maze(walls, start, goal)

		val robot = Robot(start, 0.deg, 5.0)

		val setting = SimulationSetting()

		fun solutionSort(population: List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>>): List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>> {
			return population.sortedByDescending {
				e -> e.behaviour!!.sum()
			}
		}

		fun problemSort(population: List<Entity<List<Double>, Maze, Double, MutableList<Double>>>): List<Entity<List<Double>, Maze, Double, MutableList<Double>>> {
			return population.sortedByDescending {
				e ->
				val sum = e.behaviour!!.sum()
				val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
				Math.abs(x!!.behaviour!!.sum() - sum)
			}
		}

		val evolver = GenericMazeEvolution(maze, setting, RnnMazeEvolution.rule(RnnMazeEvolution.Selectors(::solutionSort, ::problemSort)), solutionScore = {
			if (it.size == 0) -Double.MAX_VALUE
			else it.sum()
		})

		evolver.progressProperty().onChange {
			if (it != null) {
				val behaviour = (evolver.bestSolution.behaviour!! as List<Double>)
				val score = behaviour.sum() / behaviour.size
				val dif = evolver.solutions.sumByDouble { e ->
					val sum = (e.behaviour!! as List<Double>).sum()
					val x = evolver.solutions.filter { it != e }.minBy {
						Math.abs((it.behaviour!! as List<Double>).sum() - sum)
					}
					Math.abs((x!!.behaviour!! as List<Double>).sum() - sum) / behaviour!!.size
				} / evolver.solutions.size
				print("\r%3.1f%% %.1f %.1f".format(100*it, score, dif))
			}
		}

		val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
		mapper.registerModule(KotlinModule()) // Enable Kotlin support

		val file = File("winner.yaml")

		var computeOrLoad: Boolean? = null

		alert(Alert.AlertType.CONFIRMATION, "Load or Compute?", "Do you want to load or compute a solution.", ButtonType("Load", ButtonBar.ButtonData.NO), ButtonType("Compute",ButtonBar.ButtonData.YES), ButtonType.CANCEL ) {
			button ->
			when (button.text) {
				"Compute" -> computeOrLoad = true
				"Load" -> computeOrLoad = false
			}
		}

		if (computeOrLoad != null) {

			val debug = MatchupFragment()

			this += debug

			runAsync {
				if (computeOrLoad!!) {
					val winner = evolver.evolve(100, 100, 10000, object : StatisticDelegate<List<Double>, ARobotController, Double, MutableList<Double>, List<Double>, Maze, Double, MutableList<Double>> {
						override fun initialize(solutionCount: Int, problemCount: Int): Statistic {
							return Statistic("cycle", "score")
						}

						override fun update(stats: Statistic, generation: Int, state: EvolutionState<List<Double>, ARobotController, Double, MutableList<Double>, List<Double>, Maze, Double, MutableList<Double>>) {
							state.solutions.forEach {
								val row = stats.newRow()
								row["cycle"] = generation
								row["score"] = it.behaviour!!.sum()
							}
							val currentBestController = solutionSort(state.solutions).first().phenotype.copy()
							val currentBestMaze = problemSort(state.problems).first().phenotype

							Platform.runLater {
								debug.drawMatchup(debug.canvas.graphicsContext2D, currentBestMaze, currentBestController)
							}
						}

						override fun save(stats: Statistic) {
							File("stats.csv").writeText(stats.toCSV())
						}

					})
					val controller = winner.phenotype as RnnRobotController
					file.outputStream().use {
						mapper.writeValue(it, controller)
					}
					controller
				}
				else {
					file.inputStream().use {
						mapper.readValue(it, RnnRobotController::class.java)
					}
				}
			}.ui {
				// remove the debug window before adding the final simulation
				debug.removeFromParent()
				val state = MazeNavigationState(robot, maze, it as ARobotController)

				val frag = MazeFragment(this, state, setting)

				this += frag
			}
		} else {
			val manual = ManualFragment(this, maze, setting)
			this += manual
		}

	}
}