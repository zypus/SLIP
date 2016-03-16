package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.Selection
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.algorithms.genetic.crossover
import com.zypus.SLIP.algorithms.genetic.elitist
import com.zypus.SLIP.algorithms.genetic.mutate
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.utilities.Vector2

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 03/03/16
 */

class SpringEvolution {

	val evolutionRule = evolution<Long, SpringController, Double, Environment, Environment, Double> {

		solution = {

			initialize = { (2 * (Math.random() - 0.5) * Long.MAX_VALUE).toLong() }

			mapping = { gen -> SpringController { slip -> (gen.toDouble() / 0x3fffffffL) * slip.velocity.x + 0.1 } }

			select = { population ->
				val picked = elitist(population, 10) {
					it.behavior.values.sum()
				}
				Selection((0..4).map { picked[2 * it] to picked[2 * it + 1] }, population.filter { e -> !picked.contains(e) })
			}

			reproduce = { mother, father ->
				mother.crossover(father, 1.0).mutate(2.0)
			}

		}

		singularProblem = Environment { 30.0 }

		test = {

			val initial = Initial(position = Vector2(0, 210))

			val setting = SimulationSetting()

			match = {
				evolutionState ->
				evolutionState.solutions.flatMap { s -> evolutionState.problems.map { p -> s to p } }
			}

			evaluate = {
				controller, environment ->
				var state = SimulationState(SLIP(initial).copy(controller = controller), environment)
				for (i in 1..4000) {
					state = SimulationController.step(state, setting)
				}
				val x = state.slip.headPosition.x
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

		return first.phenotype
	}

}
