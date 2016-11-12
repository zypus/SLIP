package com.zypus.Maze.algorithms

import com.zypus.Maze.controller.ARobotController
import com.zypus.Maze.controller.LinearRobotController
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.Robot
import com.zypus.Maze.simulation.MazeNavigation
import com.zypus.SLIP.algorithms.SortLock
import com.zypus.SLIP.algorithms.genetic.Selection
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.algorithms.genetic.crossover
import com.zypus.SLIP.algorithms.genetic.linearSelection
import com.zypus.SLIP.algorithms.genetic.mutate
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.utilities.LineSegment
import com.zypus.utilities.deg
import com.zypus.utilities.distanceTo
import mikera.vectorz.Vector2
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 27/04/16
 */

object SimpleMazeEvolution {

	val setting = SimulationSetting()

	val rule = evolution<List<Double>, ARobotController, Double, MutableList<Double>, Maze, Maze, Double, MutableList<Double>> {

		val random = Random()

		val solutionBounds = arrayListOf(
				-1.0 to 1.0
		)

		val resolveBound = fun(bound: Pair<Double, Double>): Double {
			return if (bound.first == Double.NEGATIVE_INFINITY && bound.second == Double.POSITIVE_INFINITY) {
				Double.MAX_VALUE * random.nextDouble() * if (random.nextBoolean()) 1 else -1
			}
			else {
				(bound.second - bound.first) * random.nextDouble() + bound.first
			}
		}

		fun crossOverMutation(mother: List<Double>, father: List<Double>, cRate: Double, mRate: Double, sRate: Double, change: Double, bounds: List<Pair<Double, Double>>): List<Double> {
			val crossover = mother.crossover(father, cRate)
			return crossover.mutate(mRate) {
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

		/* MARK: Solution */

		val historySize = 20

		/* Model of the spring controller: controller controls the angle of the spring while in flight phase and controls the spring constant in stance phase. */
		solution = {

			initialize = {
				(1..22).mapIndexed { i, v ->
					// Either use the bound that is provided for the index, or use first bound (assumed to be the bound for all indices)
					resolveBound(solutionBounds.getOrElse(i) { solutionBounds[0] })
				}
			}

			mapping = {
				gen ->
				val leftWeights = gen.slice(0..10)
				val rightWeights = gen.slice(11..21)
				LinearRobotController(leftWeights, rightWeights)
			}

			select = { population ->
				val rankedPopulation = population.sortedByDescending { e ->
					val sum = e.behaviour!!.sum()
					val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
					Math.abs(x!!.behaviour!!.sum() - sum)
				}
				Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5, random) to rankedPopulation.linearSelection(1.5, random)))
			}

			refine = {
				el, n ->
				synchronized(SortLock.lock) {
					el.toList().sortedByDescending { e->
						val sum = e.behaviour!!.sum()
						val x = el.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
						Math.abs(x!!.behaviour!!.sum() - sum)
					}.take(n)
				}
			}

			reproduce = { mother, father ->
				crossOverMutation(mother, father, cRate = 1.0, mRate = 1.0, sRate = 0.4, change = 0.001, bounds = solutionBounds)
			}

			behaviour = {

				initialize = { arrayListOf<Double>() as MutableList<Double> }

				store = {
					e, o, b ->
					val ne: MutableList<Double> = arrayListOf(*e.toTypedArray(), b)
					ne.takeLast(historySize) as MutableList<Double>
				}

			}

		}

		/* MARK: Problem */

		problem = {
			initialize = {
				val walls = arrayListOf(
						LineSegment(Vector2(0.0, 0.0), Vector2(0.0, 500.0)),
						LineSegment(Vector2(0.0, 500.0), Vector2(500.0, 500.0)),
						LineSegment(Vector2(0.0, 0.0), Vector2(500.0, 0.0)),
						LineSegment(Vector2(500.0, 0.0), Vector2(500.0, 500.0)),
						LineSegment(Vector2(100.0, 400.0), Vector2(400.0, 100.0))
				)

				val start = Vector2(100.0, 100.0)
				val goal = Vector2(400.0, 400.0)

				Maze(walls, start, goal)}

			behaviour = {
				initialize = { arrayListOf() }
				store = {
					e, o, b ->
					val ne: MutableList<Double> = arrayListOf(*e.toTypedArray(), b)
					ne.takeLast(historySize) as MutableList<Double>
				}
			}
		}

//		val sinusComponentCount = 3
//
//		val problemBounds = arrayListOf(
//				/* Flat component */
//				0.0 to 50.0            /* height */
//				/* Sinus components */
//		) + (1..sinusComponentCount).flatMap {
//			arrayListOf(
//					0.001 to 0.2, /* frequency */
//					0.0 to 10.0, /* amplitude */
//					0.0 to 2 * PI   /* shift */
//			)
//		}
//
//		problem = {
//
//			/* Initialize according to the bounds above. */
//			initialize = {
//				problemBounds.map {
//					resolveBound(it)
//				}
//			}
//
//			mapping = { gen ->
//				val components = arrayListOf<Terrain>(
//						FlatTerrain(gen[0])
//				) + (0..sinusComponentCount - 1).map {
//					SinusTerrain(gen[1 + it * 3], gen[2 + it * 3], gen[3 + it * 3])
//				}
//				Environment(
//						terrain = CompositeTerrain(*components.toTypedArray())
//				)
//			}
//
//			refine = {
//				el, n ->
//				synchronized(SortLock.lock) {
//					el.toList().sortedByDescending {
//						it.behaviour!!.sum()
//					}.take(n)
//				}
//			}
//
//			select = { population ->
//				val rankedPopulation = population.sortedByDescending { e ->
//					val sum = e.behaviour!!.sum()
//					val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
//					Math.abs(x!!.behaviour!!.sum() - sum)
//				}
//				Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5, random) to rankedPopulation.linearSelection(1.5, random)))
//			}
//
//			reproduce = { mother, father ->
//				crossOverMutation(mother, father, cRate = 1.0, mRate = 1.0, sRate = 0.4, change = 0.001, bounds = problemBounds)
//			}
//
//			behaviour = {
//
//				initialize = { arrayListOf<Double>() }
//
//				store = {
//					e, o, b ->
//					e.add(b)
//					e.takeLast(historySize) as MutableList<Double>
//				}
//
//			}
//
//		}
//
//		/* MARK: Evaluation */
//
		val testRuns = 20

		test = {

			match = {
				evolutionState ->
				synchronized(SortLock.lock) {
					val sortedSolutions = evolutionState.solutions.sortedByDescending { e ->
						val sum = e.behaviour!!.sum()
						val x = evolutionState.problems.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
						Math.abs(x!!.behaviour!!.sum() - sum)
					}
					val sortedProblems = if (evolutionState.problems.size == 1) evolutionState.problems else evolutionState.problems.sortedByDescending { e ->
						val sum = e.behaviour!!.sum()
						val x = evolutionState.problems.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
						Math.abs(x!!.behaviour!!.sum() - sum)
					}
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
				val robot = Robot(maze.start.clone(), 0.deg, 5.0)
				var state = MazeNavigationState(robot = robot,maze = maze, controller = controller)
				for (i in 1..2000) {
					state = MazeNavigation.step(state, setting)
//					if (state.slip.crashed) break
				}
				val x = maze.goal distanceTo state.robot.pos
				/* Positive feedback for the solution, negative feedback for the problem. */
				x to -x
			}

		}

	}

}