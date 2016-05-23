package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.Selection
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.algorithms.genetic.crossover
import com.zypus.SLIP.algorithms.genetic.linearSelection
import com.zypus.SLIP.algorithms.genetic.mutate
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.MidpointTerrain
import com.zypus.utilities.Vector2
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 27/04/16
 */

object Coevolution2 {

	val initial = Initial()
	val setting = SimulationSetting()

	val rule = evolution<List<Double>, SpringController, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>> {

		val random = Random()

		val solutionBounds = arrayListOf(-0.5 to 0.5, -0.5 to 0.5, 0.1 to 1.0, 0.1 to 5.0)

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
				(1..4).mapIndexed { i, v ->
					// Either use the bound that is provided for the index, or use first bound (assumed to be the bound for all indices)
					resolveBound(solutionBounds.getOrElse(i) { solutionBounds[0] })
				}
			}

			mapping = { gen -> SpringController ({ slip -> gen[0] * slip.velocity.x + gen[1] }, { slip -> gen[2] * (1.0 - (slip.length / slip.restLength)) + gen[3] }) }


			select = { population ->
				val rankedPopulation = population.sortedByDescending { it.behaviour!!.sum() }
				Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5) to rankedPopulation.linearSelection(1.5)))
			}

			refine = {
				el, n ->
				synchronized(SortLock.lock) {
					el.toList().sortedByDescending {
						it.behaviour!!.sum()
					}.take(n)
				}
			}

			reproduce = { mother, father ->
				crossOverMutation(mother, father, cRate = 1.0, mRate = 1.0, sRate = 0.4, change = 0.001, bounds = solutionBounds)
			}

			behaviour = {

				initialize = { arrayListOf<Double>() }

				store = {
					e, o, b ->
					e.add(b)
					e.takeLast(historySize) as MutableList<Double>
				}

			}

		}

		/* MARK: Problem */

		val problemBounds = arrayListOf(
				0.0 to 10.0,
				0.0 to 50.0,
				0.0 to 1.0,
				0.0 to 50.0
		)

		problem =
				{

					/* Initialize according to the bounds above. */
					initialize = {
						problemBounds.map {
							resolveBound(it)
						}
					}

					mapping = { gen ->
						Environment(
								terrain = MidpointTerrain(gen[0].toInt(), gen[1], gen[2], gen[3])
						)
					}

		refine = {
			el, n ->
			synchronized(SortLock.lock) {
				el.toList().sortedByDescending {
					it.behaviour!!.sum()
				}.take(n)
			}
		}

		select = { population ->
			val rankedPopulation = population.sortedByDescending { it.behaviour!!.sum() }
			Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5) to rankedPopulation.linearSelection(1.5)))
		}

		reproduce = { mother, father ->
			crossOverMutation(mother, father, cRate = 1.0, mRate = 1.0, sRate = 0.4, change = 0.001, bounds = problemBounds)
		}

		behaviour = {

			initialize = { arrayListOf<Double>() }

			store = {
				e, o, b ->
				e.add(b)
				e.takeLast(historySize) as MutableList<Double>
			}

		}

	}

	/* MARK: Evaluation */

	val testRuns = 20

	test =
	{

		match = {
			evolutionState ->
			synchronized(SortLock.lock) {
				val sortedSolutions = evolutionState.solutions.sortedByDescending { it.behaviour!!.sum() }
				val sortedProblems = evolutionState.problems.sortedByDescending { it.behaviour!!.sum() }
//					val totalSolutionFitness = sortedSolutions.sumByDouble { it.behaviour!!.sum() }
//					val totalProblemFitness = sortedProblems.sumByDouble { it.behaviour!!.sum() }
				evolutionState.solutions.filter { it.behaviour!!.size == 0 }.flatMap {
					s ->
					(1..historySize).map { s to sortedProblems.linearSelection(1.5) }
//						sortedProblems.take(historySize).map { s to it }
				} + evolutionState.problems.filter { it.behaviour!!.size == 0 }.flatMap {
					p ->
					(1..historySize).map { sortedSolutions.linearSelection(1.5) to p }
//						sortedSolutions.take(historySize).map { it to p }
				} + (1..testRuns).map {
					sortedSolutions.linearSelection(1.5) to sortedProblems.linearSelection(1.5)
//						var current = 1.0
//						val nextSolution = random.nextDouble()
//						val s = sortedSolutions.find {
//							current -= it.behaviour!!.sum() / totalSolutionFitness
//							nextSolution > current
//						} ?: sortedSolutions.first()
//						current = 1.0
//						val nextProblem = random.nextDouble()
//						val p = sortedProblems.find {
//							current -= it.behaviour!!.sum() / totalProblemFitness
//							nextProblem > current
//						} ?: sortedProblems.first()
//						s to p
				}
			}
		}

		evaluate = {
			controller, environment ->
			val ix = random.nextDouble() * 40.0 - 20.0
			var state = SimulationState(SLIP(Initial(Vector2(ix, 200))).copy(controller = controller), environment.copy(terrain = (environment.terrain as MidpointTerrain).copy()))
			for (i in 1..2000) {
				state = SimulationController.step(state, DiversityCoevolution2.setting)
				if (state.slip.crashed) break
			}
			val x = state.slip.position.x - ix
			/* Positive feedback for the solution, negative feedback for the problem. */
			x to -x
		}

	}

}

}