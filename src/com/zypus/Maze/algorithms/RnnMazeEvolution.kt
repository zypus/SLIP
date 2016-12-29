package com.zypus.Maze.algorithms

import com.zypus.Maze.controller.ARobotController
import com.zypus.Maze.controller.DeepRnnRobotController
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.Robot
import com.zypus.Maze.simulation.MazeNavigation
import com.zypus.SLIP.algorithms.SortLock
import com.zypus.SLIP.algorithms.genetic.*
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.utilities.*
import mikera.vectorz.Vector2
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 27/04/16
 */

object RnnMazeEvolution {

	data class Selectors(
			val solutionSelection: (List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>>) -> List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>>,
			val problemSelection: (List<Entity<List<Double>, Maze, Double, MutableList<Double>>>) -> List<Entity<List<Double>, Maze, Double, MutableList<Double>>>,
			val solutionRemoval: (List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>>) -> List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>> = solutionSelection,
			val solutionMatching: (List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>>) -> List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>> = solutionSelection,
			val problemRemoval: (List<Entity<List<Double>, Maze, Double, MutableList<Double>>>) -> List<Entity<List<Double>, Maze, Double, MutableList<Double>>> = problemSelection,
			val problemMatching: (List<Entity<List<Double>, Maze, Double, MutableList<Double>>>) -> List<Entity<List<Double>, Maze, Double, MutableList<Double>>> = problemSelection
	)

	val setting = SimulationSetting(0.05)

	fun rule(selectors: Selectors) = evolution<List<Double>, ARobotController, Double, MutableList<Double>, List<Double>, Maze, Double, MutableList<Double>> {

		val random = Random()

		val solutionBounds = arrayListOf(
				-10.0 to 10.0
		)

		val resolveBound = fun(bound: Pair<Double, Double>): Double {
			return if (bound.first == Double.NEGATIVE_INFINITY && bound.second == Double.POSITIVE_INFINITY) {
				Double.MAX_VALUE * random.nextDouble() * if (random.nextBoolean()) 1 else -1
			}
			else {
				(bound.second - bound.first) * random.nextDouble() + bound.first
			}
		}

		fun kernel(value: Double): Double {
			if (Math.abs(value) > 0.5) {
				if (value < 0.0) {
					return value + 0.5
				} else {
					return value - 0.5
				}
			} else {
				return 0.0
			}
		}

		fun crossOverMutation(mother: List<Double>, father: List<Double>, cRate: Double, mRate: Double, sRate: Double, change: Double, bounds: List<Pair<Double, Double>>): List<Double> {
			val crossover = mother.crossover(father, cRate)
			return crossover.mutate(mRate, normalize = false) {
				i, e ->
				val bound = if (bounds.size > i) {
					bounds[i]
				}
				else {
					bounds[0]
				}
				when (random.nextDouble()) {
				// decrease/increase the value a bit
					in 0.0..(1.0 - sRate) / 2           -> {
						Math.max(bound.first, e - change)
					}
					in (1.0 - sRate) / 2..(1.0 - sRate) -> {
						Math.min(e + change, bound.second)
					}
					else                                -> {
						resolveBound(bound)
					}
				}
			}.toList()
		}

		fun createProxies(data: List<Double>, layer: List<Int>): List<MatrixProxy> {
			val count = layer.size-2
			val secondToLast = layer[layer.size - 2]
			val last = layer[layer.size - 1]
			val yStart = data.size - (secondToLast+1)*last
			val Why = MatrixProxy(data.slice(yStart..(yStart + secondToLast*last - 1)), secondToLast, last)
			val by = MatrixProxy(data.slice(yStart + secondToLast * last..(data.size-1)), 1, last)
			val proxies = (1..count).flatMap {
				val (inputs, hidden) = layer.slice((it-1)..it)
				val WxhEnd = inputs * hidden
				val WhhEnd = WxhEnd + hidden * hidden
				val genSize = WhhEnd + hidden
				val gen = data.slice(genSize * (it - 1)..(genSize * it) - 1)
				val Wxh = MatrixProxy(gen.slice(0..(WxhEnd - 1)), inputs, hidden)
				val Whh = MatrixProxy(gen.slice(WxhEnd..(WhhEnd - 1)), hidden, hidden)
				val bh = MatrixProxy(gen.slice(WhhEnd..(WhhEnd + hidden - 1)), 1, hidden)
				val ih = MatrixProxy(Array(hidden) { 0.0 }.toList(), 1, hidden)
				arrayListOf(ih, Wxh, Whh, bh)
			} + arrayListOf(Why, by)
			return proxies
		}

		/* MARK: Solution */

		val mazeSize = 50.0

		val historySize = 20

		val layer = arrayListOf(10, 16, 2)

		val size = (1..layer.size-2).fold((layer[layer.size-2]+1)*layer[layer.size-1]) {
			x, count -> val (inputs, hidden) = layer.slice((count - 1)..count)
			x + inputs*hidden + hidden*hidden + hidden
		}

		/* Model of the spring controller: controller controls the angle of the spring while in flight phase and controls the spring constant in stance phase. */
		solution = {

			initialize = {
				(1..size).mapIndexed { i, v ->
					// Either use the bound that is provided for the index, or use first bound (assumed to be the bound for all indices)
					kernel(resolveBound(solutionBounds.getOrElse(i) { solutionBounds[0] }))
				}
			}

			mapping = {
				gen ->
				DeepRnnRobotController(createProxies(gen, layer))
			}

			select = { population ->
				val rankedPopulation = selectors.solutionSelection(population)
				Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5, random) to rankedPopulation.linearSelection(1.5, random)))
			}

			refine = {
				el, n ->
				synchronized(SortLock.lock) {
					val sorted = selectors.solutionRemoval(el).reversed()
					val unlucky = sorted.linearSelection(1.5,random)
					sorted.filter { it != unlucky }
				}
			}

			reproduce = { mother, father ->
				crossOverMutation(mother, father, cRate = 0.0, mRate = 0.05, sRate = 0.4, change = 0.05, bounds = solutionBounds).map(::kernel)
			}

			behaviour = {

				initialize = { arrayListOf<Double>() }

				store = {
					e, o, b ->
					val ne: MutableList<Double> = arrayListOf(*e.toTypedArray(), b)
					ne.takeLast(historySize) as MutableList<Double>
				}

			}

		}

		/* MARK: Problem */

		val problemBounds = arrayListOf(
				0.1 to 0.9,
				-180.0 to 180.0,
				mazeSize * 0.02 to mazeSize * 0.98,
				mazeSize * 0.02 to mazeSize * 0.98,
				mazeSize * 0.02 to mazeSize * 0.98,
				mazeSize * 0.02 to mazeSize * 0.98
		)

		problem = {

			/* Initialize according to the bounds above. */
			initialize = {
				(0..8).mapIndexed { i, v ->
					// Either use the bound that is provided for the index, or use first bound (assumed to be the bound for all indices)
					resolveBound(problemBounds.getOrElse(i) { problemBounds[0] })
				}
			}

			mapping = { gen ->
				val walls = arrayListOf(*arrayOf(
						LineSegment(Vector2(0.0, 0.0), Vector2(0.0, mazeSize)),
						LineSegment(Vector2(0.0, mazeSize), Vector2(mazeSize, mazeSize)),
						LineSegment(Vector2(0.0, 0.0), Vector2(mazeSize, 0.0)),
						LineSegment(Vector2(mazeSize, 0.0), Vector2(mazeSize, mazeSize))),
						*gen
						.drop(6) // ignore the first 5 entries
						.mapIndexed { i: Int, d: Double -> i to d } // attach an index to each
						.groupBy { it.first / 3 } // group them by 3
						.map { // map each 3 elements to a line
							val values = it.value.map { it.second }
							val center = Vector2(gen[2], gen[3])
							center.addMultiple(Vector2(gen[4]- gen[2], gen[5]- gen[3]), values[0])
							val dir = Vector2(1.0, 0.0)
							dir.rotate((values[1]*360).deg)
							val start = center.addMultipleCopy(dir, values[2]*mazeSize/2)
							dir.negate()
							val end = center.addMultipleCopy(dir, values[2]*mazeSize/2)
							LineSegment(start as Vector2, end as Vector2)
						}.toTypedArray())
				Maze(walls, start = Vector2(gen[2], gen[3]), goal = Vector2(gen[4], gen[5]), orientation = gen[1])
			}

			refine = {
				el, n ->
				synchronized(SortLock.lock) {
					val sorted = selectors.problemRemoval(el).reversed()
					val unlucky = sorted.linearSelection(1.5, random)
					sorted.filter { it != unlucky }
				}
			}

			select = { population ->
				val rankedPopulation = selectors.problemSelection(population)
				Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5, random) to rankedPopulation.linearSelection(1.5, random)))
			}

			reproduce = { mother, father ->
				crossOverMutation(mother, father, cRate = 1.0, mRate = 0.05, sRate = 0.4, change = 0.05, bounds = problemBounds)
			}

			behaviour = {

				initialize = { arrayListOf<Double>() }

				store = {
					e, o, b ->
					val ne: MutableList<Double> = arrayListOf(*e.toTypedArray(), b)
					ne.takeLast(historySize) as MutableList<Double>
				}

			}

		}

		/* MARK: Evaluation */

		val testRuns = 5

		test = {

			match = {
				evolutionState ->
				synchronized(SortLock.lock) {
					val sortedSolutions = selectors.solutionMatching(evolutionState.solutions)
					val sortedProblems = selectors.problemMatching(evolutionState.problems)
					evolutionState.solutions.filter { it.behaviour!!.size == 0 }.flatMap {
						s ->
						(1..historySize).map { s to sortedProblems.linearSelection(1.5, random) }
					} + evolutionState.problems.filter { it.behaviour!!.size == 0 }.flatMap {
						p ->
						(1..historySize).map { sortedSolutions.linearSelection(1.5, random) to p }
					} + (1..testRuns).map {
						sortedSolutions.linearSelection(1.5, random) to sortedProblems.linearSelection(1.5, random)
					}
				}
			}

			evaluate = {
				controller, maze ->
				val robot = Robot(maze.start.clone(), maze.orientation.deg, 0.01* mazeSize)
				val actualControl = controller.copy()
				actualControl.start()
				var state = MazeNavigationState(robot = robot, maze = maze, controller = actualControl)
				var travelledDistance = 0.0
				var reachedGoal = false
				var iterationsNeeded = 500
				var goalPoints = 0
				for (i in 1..500) {
					val before = state.robot.pos.clone()
					state = MazeNavigation.step(state, setting)
					if (!reachedGoal) {
						travelledDistance += before distanceTo state.robot.pos
					}
					if (!reachedGoal && maze.goal distanceTo state.robot.pos < robot.radius) {
						reachedGoal = true
						iterationsNeeded = i
					} else if (reachedGoal && maze.goal distanceTo state.robot.pos < robot.radius) {
						goalPoints += 1
					}
				}
				var distanceToGoal = maze.start distanceTo maze.goal
				if (distanceToGoal == 0.0) distanceToGoal = 1.0
				val s = if (travelledDistance == 0.0) 2000.0 else if (reachedGoal) (travelledDistance + iterationsNeeded)/distanceToGoal - goalPoints else (state.robot.pos distanceTo maze.goal) + 3*iterationsNeeded
				assert(s != Double.POSITIVE_INFINITY && s != Double.NEGATIVE_INFINITY)
				/* Positive feedback for the solution, negative feedback for the problem. */
				-s to s
			}

		}

	}

}