package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.Selection
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.algorithms.genetic.crossover
import com.zypus.SLIP.algorithms.genetic.elitist
import com.zypus.SLIP.algorithms.genetic.mutate
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 03/03/16
 */

class SpringEvolution(val initial: Initial, val environment: Environment, val setting: SimulationSetting) {

	val evolutionRule = evolution<Long, SpringController, Double, Environment, Environment, Double> {

		solution = {

			initialize = { (2 * (Math.random() - 0.5) * Long.MAX_VALUE).toLong() }

			mapping = { gen ->
				val f = (gen.toDouble() / 0x3fffffffffffffffL)
				SpringController { slip ->f * slip.velocity.x + 0.1 } }

			select = { population ->
				val picked = elitist(population, 10) {
					it.behavior.values.sum()
				}
				Selection((0..4).map { picked[2 * it] to picked[2 * it + 1] }, population.filter { e -> !picked.contains(e) })
			}

			reproduce = { mother, father ->
				mother.crossover(father, 1.0).mutate(1.0)
			}

		}

		singularProblem = environment

		test = {

			match = {
				evolutionState ->
				evolutionState.solutions.flatMap { s -> evolutionState.problems.map { p -> s to p } }
			}

			evaluate = {
				controller, environment ->
				var state = SimulationState(SLIP(initial).copy(controller = controller), environment)
				for (i in 1..10000) {
					state = SimulationController.step(state, setting)
					if (state.slip.position.y - state.slip.radius <= state.environment.terrain(state.slip.position.x)) break
				}
				val x = state.slip.position.x
				x to x
			}

		}

	}

	fun evolve(): SpringController {
		var state = evolutionRule.initialize(300, 1)

		for (i in 1..100) {
			evolutionRule.matchAndEvaluate(state)
			state = evolutionRule.selectAndReproduce(state)
		}

		evolutionRule.matchAndEvaluate(state)

		val first = state.solutions.sortedByDescending { it.behavior.values.sum() }.first()

		println(state.solutions.first().genotype)

		return first.phenotype
	}

}

class SpringEvolution2(val initial: Initial, val environment: Environment, val setting: SimulationSetting) {

	val evolutionRule = evolution<List<Double>, SpringController, Double, Environment, Environment, Double> {

		val random = Random()

		val bounds = arrayListOf(-0.5 to 0.5)

		val resolveBound = fun(bound: Pair<Double, Double>): Double {
			return if (bound.first == kotlin.Double.NEGATIVE_INFINITY && bound.second == kotlin.Double.POSITIVE_INFINITY) {
				kotlin.Double.MAX_VALUE * random.nextDouble() * if (random.nextBoolean()) 1 else -1
			}
			else {
				bound.second - bound.first * random.nextDouble() + bound.first
			}
		}

		solution = {

			initialize = {
				(1..2).mapIndexed { i, v ->
					// Either use the bound that is provided for the index, or use first bound (assumed to be the bound for all indices)
					val bound = if (bounds.size > i) {
						bounds[i]
					}
					else {
						bounds[0]
					}
					resolveBound(bound)
				}
			}

			mapping = { gen -> SpringController { slip -> gen[0] * slip.velocity.x + gen[1] } }

			select = { population ->
				val picked = elitist(population, 10) {
					it.behavior.values.sum()
				}
				Selection((0..4).map { picked[2 * it] to picked[2 * it + 1] }, population.filter { e -> !picked.contains(e) })
			}

			reproduce = { mother, father ->
				val crossover = mother.crossover(father, 1.0)
				crossover.mutate(1.0) {
					i, e ->
					val bound = if (bounds.size > i) {
						bounds[i]
					}
					else {
						bounds[0]
					}
					when (random.nextDouble()) {
					// decrease/increase the value a bit
						in 0.0..0.2 -> {
							Math.max(bound.first, e - 0.001)
						}
						in 0.2..0.4 -> {
							Math.min(e + 0.001, bound.second)
						}
					// decrease/increase in percent
//						in 0.6..0.7 -> {
//							Math.max(bound.first, Math.min(e * 90.percent, bound.second))
//						}
//						in 0.7..0.8 -> {
//							Math.max(bound.first ,Math.min(e * 110.percent, bound.second))
//						}
					// assign a new random value, which is in the bounds
						else        -> {
							resolveBound(bound)
						}
					}
				}.toList()
			}

		}

		singularProblem = environment

		test = {

			match = {
				evolutionState ->
				val flatMap = evolutionState.solutions.flatMap { s -> evolutionState.problems.map { p -> s to p } }
				flatMap
			}

			evaluate = {
				controller, environment ->
				var state = SimulationState(SLIP(initial).copy(controller = controller), environment)
				for (i in 1..10000) {
					state = SimulationController.step(state, setting)
					if (state.slip.position.y-state.slip.radius <= state.environment.terrain(state.slip.position.x)) break
				}
				val x = state.slip.position.x
				x to x
			}

		}

	}

	fun evolve(): SpringController {
		var state = evolutionRule.initialize(100, 1)

		for (i in 1..100) {
			evolutionRule.matchAndEvaluate(state)
			state = evolutionRule.selectAndReproduce(state)
		}

		evolutionRule.matchAndEvaluate(state)

		val first = state.solutions.sortedByDescending { it.behavior.values.sum() }.first()

		println(state.solutions.first().genotype)

		return first.phenotype
	}

}
