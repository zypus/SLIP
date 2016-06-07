package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.*
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.MidpointTerrain
import com.zypus.utilities.pickRandom
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 24/05/16
 */
object SLIPTerrainEvolution {

	val initial = Initial()
	val setting = SimulationSetting()

	fun rule(solutionSelector: (List<Entity<List<Double>, *, Double, MutableList<Double>>>) -> (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double, problemSelector: (List<Entity<List<Double>, *, Double, MutableList<Double>>>) -> (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double, historySize: Int = 20 , testRuns: Int = 20,noiseStrength: Double = 0.0, adaptiveReproduction: Boolean = false, seed: Long = 0, replaceCount: Int = 0) =
		evolution<List<Double>, SLIP, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>> {

			val mainRandom = Random(seed)
			val slipRandom = Random(seed+16127)
			val terrainRandom = Random(seed+66089)
			val utilityRandom = Random(seed+92857)
			val replaceRandom = Random(seed+101)

			val solutionBounds = arrayListOf(
					-0.5 to 0.5,
					-0.5 to 0.5,
					0.1 to 1.0,
					0.1 to 5.0,
					10.0 to 150.0,
					0.5 to 3.0
			)

			val resolveBound = fun(bound: Pair<Double, Double>, random: Random): Double {
				return if (bound.first == Double.NEGATIVE_INFINITY && bound.second == Double.POSITIVE_INFINITY) {
					Double.MAX_VALUE * random.nextDouble() * if (random.nextBoolean()) 1 else -1
				}
				else {
					(bound.second - bound.first) * random.nextDouble() + bound.first
				}
			}

			fun crossOverMutation(mother: List<Double>, father: List<Double>, cRate: Double, mRate: Double, sRate: Double, change: Double, bounds: List<Pair<Double, Double>>, random: Random): List<Double> {
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
							resolveBound(bound,random)
						}
					}
				}.toList()
			}

			/* MARK: Solution */

			/* Model of the spring controller: controller controls the angle of the spring while in flight phase and controls the spring constant in stance phase. */
			solution = {

				initialize = {
					solutionBounds.map {
						resolveBound(it, slipRandom)
					}
				}

				fun Double.withNoise(ns: Double): Double = this + slipRandom.nextGaussian() * ns

				mapping = { gen ->
					SLIP(restLength = gen[4], mass = gen[5], radius = 10 * gen[5], controller = SpringController ({ slip -> (gen[0] * slip.velocity.x.withNoise(noiseStrength) + gen[1]).withNoise(noiseStrength/100) }, { slip -> (gen[2] * (1.0 - (slip.length.withNoise(noiseStrength) / slip.restLength)) + gen[3]).withNoise(noiseStrength/100) }))
				}

				select = { population ->
					val rankedPopulation = population.sortedByDescending(solutionSelector(population))
					val fitness = rankedPopulation.first().behaviour!!.sum()
					if (!adaptiveReproduction || utilityRandom.nextDouble() < 1-fitness/(historySize*5000)) {
						Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5, slipRandom) to rankedPopulation.linearSelection(1.5, slipRandom)),toBeReplaced = (1..replaceCount).map {rankedPopulation.pickRandom { replaceRandom.nextDouble() }})
					} else {
						Selection(0, arrayListOf(),toBeReplaced = (1..replaceCount).map {rankedPopulation.pickRandom { replaceRandom.nextDouble() }})
					}
				}

				refine = {
					el, n ->
					synchronized(SortLock.lock) {
						el.toList().sortedByDescending(solutionSelector(el)).take(n)
					}
				}

				reproduce = { mother, father ->
					crossOverMutation(mother, father, cRate = 1.0, mRate = 1.0, sRate = 0.4, change = 0.001, bounds = solutionBounds, random = slipRandom)
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
					0.0 to 50.0,
					0.0 to 100.0
			)

			problem =
					{

						/* Initialize according to the bounds above. */
						initialize = {
							problemBounds.map {
								resolveBound(it,terrainRandom)
							}
						}

						mapping = { gen ->
							Environment(
									terrain = MidpointTerrain(gen[0].toInt(), gen[1], gen[2], gen[3], gen[4].toLong())
							)
						}

						refine = {
							el, n ->
							synchronized(SortLock.lock) {
								el.toList().sortedByDescending(problemSelector(el)).take(n)
							}
						}

						select = { population ->
							val rankedPopulation = population.sortedByDescending(problemSelector(population))
							val fitness = -rankedPopulation.first().behaviour!!.sum()
							if (!adaptiveReproduction || utilityRandom.nextDouble() < fitness / (historySize * 3000)) {
								Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5,terrainRandom) to rankedPopulation.linearSelection(1.5,terrainRandom)),toBeReplaced = (1..replaceCount).map {rankedPopulation.pickRandom { replaceRandom.nextDouble() }})
							}
							else {
								Selection(0, arrayListOf(),toBeReplaced = (1..replaceCount).map {rankedPopulation.pickRandom { replaceRandom.nextDouble() }})
							}
						}

						reproduce = { mother, father ->
							crossOverMutation(mother, father, cRate = 1.0, mRate = 1.0, sRate = 0.4, change = 0.001, bounds = problemBounds, random = terrainRandom)
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

			test = {

				match = {
					evolutionState ->
					synchronized(SortLock.lock) {
						val sortedSolutions = evolutionState.solutions.filter{ it.behaviour!!.size != 0 }.sortedByDescending(solutionSelector(evolutionState.solutions))
						val sortedProblems = evolutionState.problems.filter{ it.behaviour!!.size != 0 }.sortedByDescending(problemSelector(evolutionState.problems))
						evolutionState.solutions.filter { it.behaviour!!.size == 0 }.flatMap {
							s ->
							if (sortedProblems.isEmpty()) {
								(1..historySize).map { s to evolutionState.problems.linearSelection(1.5,mainRandom) }
							} else {
								(1..historySize).map { s to sortedProblems.linearSelection(1.5,mainRandom) }
							}
						} + evolutionState.problems.filter { it.behaviour!!.size == 0 }.flatMap {
							p ->
							if (sortedSolutions.isEmpty()) {
								(1..historySize).map { evolutionState.solutions.linearSelection(1.5,mainRandom) to p}
							} else {
								(1..historySize).map { sortedSolutions.linearSelection(1.5,mainRandom) to p }
							}
						} + (1..testRuns).map {
							if (sortedSolutions.isEmpty()) {
								evolutionState.solutions.linearSelection(1.5,mainRandom) to evolutionState.problems.linearSelection(1.5,mainRandom)
							} else {
								sortedSolutions.linearSelection(1.5,mainRandom) to sortedProblems.linearSelection(1.5,mainRandom)							}
						}
					}
				}

				evaluate = {
					slip, environment ->
					val ix = mainRandom.nextDouble() * 40.0 - 20.0
					var state = SimulationState(slip.copy(position = initial.position, velocity = initial.velocity), environment.copy(terrain = (environment.terrain as MidpointTerrain).copy()))
					//var jumps = 0
					var cycle = 0
					while (cycle < 2000) {
						//val before = state.slip.grounded
						state = SimulationController.step(state, setting)
					//	if (before == true && state.slip.grounded == false) jumps++
						if (state.slip.crashed) break
						cycle++
					}
					val x = state.slip.position.x - ix
					/* Positive feedback for the solution, negative feedback for the problem. */
					x to -x
				}

			}

	}

}