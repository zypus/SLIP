package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.*
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.MidpointTerrain
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 24/05/16
 */
object SLIPTerrainEvolution {

	data class Selectors(
			val solutionSelection: (List<Entity<List<Double>, *, Double, MutableList<Double>>>) -> (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double,
			val problemSelection: (List<Entity<List<Double>, *, Double, MutableList<Double>>>) -> (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double,
			val solutionRemoval: (List<Entity<List<Double>, *, Double, MutableList<Double>>>) -> (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double = solutionSelection,
			val solutionMatching: (List<Entity<List<Double>, *, Double, MutableList<Double>>>) -> (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double = solutionSelection,
			val problemRemoval: (List<Entity<List<Double>, *, Double, MutableList<Double>>>) -> (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double = problemSelection,
			val problemMatching: (List<Entity<List<Double>, *, Double, MutableList<Double>>>) -> (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double = problemSelection
	)

	data class SLIPTerrainEvolutionSetting(val historySize: Int = 20,
										   val testRuns: Int = 20,
										   val noiseStrength: Double = 0.0,
										   val adaptiveReproduction: Boolean = false,
										   val seed: Long = 0L)

	val initial = Initial()
	val setting = SimulationSetting(simulationStep = 0.3)

	fun rule(
			selectors: Selectors,
			settings: SLIPTerrainEvolutionSetting) =
			evolution<List<Double>, SLIP, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>> {

				val mainRandom = Random(settings.seed)
				val slipRandom = Random(settings.seed + 16127)
				val terrainRandom = Random(settings.seed + 66089)
				val utilityRandom = Random(settings.seed + 92857)

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
								resolveBound(bound, random)
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
						SLIP(restLength = gen[4], mass = gen[5], radius = 10 * gen[5], controller = SpringController({ slip -> (gen[0] * slip.velocity.x.withNoise(settings.noiseStrength) + gen[1]).withNoise(settings.noiseStrength / 100) }, { slip -> (gen[2] * (1.0 - (slip.length.withNoise(settings.noiseStrength) / slip.restLength)) + gen[3]).withNoise(settings.noiseStrength / 100) }))
					}

					select = { population ->
						val rankedPopulation = population.sortedByDescending(selectors.solutionSelection(population))
						val fitness = rankedPopulation.first().behaviour!!.sum()
						if (!settings.adaptiveReproduction || utilityRandom.nextDouble() < 1 - fitness / (settings.historySize * 5000)) {
							Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5, slipRandom) to rankedPopulation.linearSelection(1.5, slipRandom)))
						}
						else {
							Selection(0, arrayListOf())
						}
					}

					refine = {
						el, n ->
						synchronized(SortLock.lock) {
							el.toList().sortedByDescending(selectors.solutionRemoval(el)).take(n)
						}
					}

					reproduce = { mother, father ->
						crossOverMutation(mother, father, cRate = 1.0, mRate = 1.0, sRate = 0.4, change = 0.001, bounds = solutionBounds, random = slipRandom)
					}

					behaviour = {

						initialize = { arrayListOf<Double>() }

						store = {
							e, o, b ->
							val ne: MutableList<Double> = arrayListOf(*e.toTypedArray(), b)
							ne.takeLast(settings.historySize) as MutableList<Double>
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
									resolveBound(it, terrainRandom)
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
									el.toList().sortedByDescending(selectors.problemRemoval(el)).take(n)
								}
							}

							select = { population ->
								val rankedPopulation = population.sortedByDescending(selectors.problemSelection(population))
								val fitness = -rankedPopulation.first().behaviour!!.sum()
								if (!settings.adaptiveReproduction || utilityRandom.nextDouble() < fitness / (settings.historySize * 3000)) {
									Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5, terrainRandom) to rankedPopulation.linearSelection(1.5, terrainRandom)))
								}
								else {
									Selection(0, arrayListOf())
								}
							}

							reproduce = { mother, father ->
								crossOverMutation(mother, father, cRate = 1.0, mRate = 1.0, sRate = 0.4, change = 0.001, bounds = problemBounds, random = terrainRandom)
							}

							behaviour = {

								initialize = { arrayListOf<Double>() }

								store = {
									e, o, b ->
									val ne: MutableList<Double> = arrayListOf(*e.toTypedArray(), b)
									ne.takeLast(settings.historySize) as MutableList<Double>}

							}

						}

				/* MARK: Evaluation */

				test = {

					match = {
						evolutionState ->
						synchronized(SortLock.lock) {
							val sortedSolutions = evolutionState.solutions.filter { it.behaviour!!.size != 0 }.sortedByDescending(selectors.solutionMatching(evolutionState.solutions))
							val sortedProblems = evolutionState.problems.filter { it.behaviour!!.size != 0 }.sortedByDescending(selectors.problemMatching(evolutionState.problems))
							evolutionState.solutions.filter { it.behaviour!!.size == 0 }.flatMap {
								s ->
								if (sortedProblems.isEmpty()) {
									(1..settings.historySize).map { s to evolutionState.problems.linearSelection(1.5, mainRandom) }
								}
								else {
									(1..settings.historySize).map { s to sortedProblems.linearSelection(1.5, mainRandom) }
								}
							} + evolutionState.problems.filter { it.behaviour!!.size == 0 }.flatMap {
								p ->
								if (sortedSolutions.isEmpty()) {
									(1..settings.historySize).map { evolutionState.solutions.linearSelection(1.5, mainRandom) to p }
								}
								else {
									(1..settings.historySize).map { sortedSolutions.linearSelection(1.5, mainRandom) to p }
								}
							} + (1..settings.testRuns).map {
								if (sortedSolutions.isEmpty()) {
									evolutionState.solutions.linearSelection(1.5, mainRandom) to evolutionState.problems.linearSelection(1.5, mainRandom)
								}
								else {
									sortedSolutions.linearSelection(1.5, mainRandom) to sortedProblems.linearSelection(1.5, mainRandom)
								}
							}
						}
					}

					evaluate = {
						slip, environment ->
						val ix = mainRandom.nextDouble() * 40.0 - 20.0
						var state = SimulationState(slip.copy(position = initial.position.clone(), velocity = initial.velocity.clone()), environment.copy(terrain = (environment.terrain as MidpointTerrain).copy()))
						var jumps = 0
						while (jumps < 50) {
							val before = state.slip.grounded
							state = SimulationController.step(state, setting)
							if (before == true && state.slip.grounded == false) jumps++
							if (state.slip.crashed) break
						}
						val x = state.slip.position.x - ix
						/* Positive feedback for the solution, negative feedback for the problem. */
						x to -x
					}

				}

			}

}