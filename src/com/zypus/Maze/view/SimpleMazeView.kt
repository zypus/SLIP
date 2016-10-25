package com.zypus.Maze.view

import com.zypus.Maze.algorithms.GenericMazeEvolution
import com.zypus.Maze.algorithms.SimpleMazeEvolution
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.Robot
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.utilities.LineSegment
import com.zypus.utilities.Vector2
import com.zypus.utilities.deg
import javafx.geometry.Insets
import tornadofx.View
import tornadofx.hbox
import tornadofx.onChange
import tornadofx.plusAssign

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
				LineSegment(Vector2(0, 0), Vector2(0, 500)),
				LineSegment(Vector2(0, 500), Vector2(500, 500)),
				LineSegment(Vector2(0, 0), Vector2(500, 0)),
				LineSegment(Vector2(500, 0), Vector2(500, 500))
		)

		val start = Vector2(100, 100)
		val goal = Vector2(400, 400)

		val maze = Maze(walls, start, goal)

		val robot = Robot(start, 0.deg, 5.0)

		val setting = SimulationSetting()

		val evolver = GenericMazeEvolution(maze, setting, SimpleMazeEvolution.rule, solutionScore = {
			-it.sum()
		})

		evolver.progressProperty().onChange {
			if (it != null) print("\r%3.1f%%".format(100*it))
		}

		val winner = evolver.evolve(25, 1, 1000)

		val state = MazeNavigationState(robot, maze, winner.phenotype)

		val frag = MazeFragment(state, setting)

		this += frag

	}
}