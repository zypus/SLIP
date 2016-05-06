package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.Selection
import com.zypus.SLIP.algorithms.genetic.Selections
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.algorithms.genetic.crossover
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

	val evolutionRule = evolution<Long, SpringController, Double, HashMap<Any,Double>, Environment, Environment, Double, HashMap<Any, Double>> {

		solution = {

			initialize = { (2 * (Math.random() - 0.5) * Long.MAX_VALUE).toLong() }

			mapping = { gen ->
				val f = (gen.toDouble() / 0x3fffffffffffffffL)
				SpringController ({ slip -> f * slip.velocity.x + 0.1 }) }

			select = { population ->
				val picked = Selections.elitist(population, 10) {
					(it.behaviour as HashMap<*, *>).values.sumByDouble { it as Double }
				}
				val toBeReplaced = population.filter { e -> !picked.contains(e) }
				Selection(toBeReplaced.size, (0..4).map { picked[2 * it] to picked[2 * it + 1] }, toBeReplaced)
			}

			reproduce = { mother, father ->
				mother.crossover(father, 1.0).mutate(1.0)
			}

			behaviour = {
				initialize = { hashMapOf() }
				store = { e,o,b -> e[o.genotype] = b; e }
				remove = {e,o -> e.remove(o.genotype); e}
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

		val first = state.solutions.sortedByDescending { (it.behaviour as HashMap<*, *>).values.sumByDouble { it as Double } }.first()

		println(state.solutions.first().genotype)

		return first.phenotype
	}

}

