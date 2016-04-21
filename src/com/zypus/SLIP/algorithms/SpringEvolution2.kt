package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.*
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.SinusTerrain
import tornadofx.getProperty
import tornadofx.property
import java.util.*

class SpringEvolution2(val initial: Initial, val environment: Environment, val setting: SimulationSetting): Evolution {

	var solutions by property<List<Entity<*,*,*>>>(emptyList())
	override fun solutionsProperty() = getProperty(SpringEvolution2::solutions)

	var generation by property<Int>()
	override fun generationProperty() = getProperty(SpringEvolution2::generation)

	var finished by property(false)
	override fun finishedProperty() = getProperty(SpringEvolution2::finished)

	var progress by property(0.0)
	override fun progressProperty() = getProperty(SpringEvolution2::progress)

	var gen = 0

	val evolutionRule = evolution<List<Double>, SpringController, Double, Double, Environment, Double> {

		val random = Random()

		val bounds = arrayListOf(-0.5 to 0.5)

		val resolveBound = fun(bound: Pair<Double, Double>): Double {
			return if (bound.first == Double.NEGATIVE_INFINITY && bound.second == Double.POSITIVE_INFINITY) {
				Double.MAX_VALUE * random.nextDouble() * if (random.nextBoolean()) 1 else -1
			}
			else {
				(bound.second - bound.first) * random.nextDouble() + bound.first
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

			mapping = { gen -> SpringController { slip -> SpringControl(gen[0] * slip.velocity.x + gen[1], slip.springConstant) } }

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

		//		singularProblem = environment

		problem = {

			initialize = { random.nextDouble() }

			mapping = { gen -> Environment (terrain = SinusTerrain(gen, 10.0, 0.0, 30.0)) }

		}

		test = {

			match = {
				evolutionState ->
				val flatMap = evolutionState.solutions.flatMap { s -> evolutionState.problems.map { p -> s to p } }
				flatMap
			}

			evaluate = {
				controller, environment ->
				var state = SimulationState(SLIP(initial).copy(controller = controller), environment)
				for (i in 1..2000) {
					state = SimulationController.step(state, setting)
					if (state.slip.position.y - state.slip.radius <= state.environment.terrain(state.slip.position.x)) break
				}
				val x = state.slip.position.x
				x to x
			}

		}

	}

	fun evolve(): Entity<List<Double>, SpringController, Double> {
		// Population settings.
		val solutionCount = 100
		val problemCount = 5

		// Evolution settings.
		val maxGenerations = 100

		// Setup statistics.
		var columns: MutableList<String> = arrayListOf("generation")
		repeat(solutionCount) {columns.add("s$it")}
		repeat(problemCount) {columns.add("p$it")}
		var stats = Statistic(*columns.toTypedArray())


		var state = evolutionRule.initialize(100, 5)

		for (g in 0..maxGenerations-1) {
			evolutionRule.matchAndEvaluate(state)

//			stats.newRow().let {
//				it["generation"] = g
//				state.solutions.forEachIndexed { i, entity -> it["s$i"] = entity.behavior.values.sum() }
//				state.problems.forEachIndexed { i, entity -> it["p$i"] = entity.behavior.values.sum() }
//			}

			solutions = state.solutions

			generation = g

			gen = g

			state = evolutionRule.selectAndReproduce(state)

			progress = g.toDouble()/maxGenerations
		}

		evolutionRule.matchAndEvaluate(state)

		stats.newRow().let {
			it["generation"] = maxGenerations
			state.solutions.forEachIndexed { i, entity -> it["s$i"] = entity.behavior.values.sum() }
			state.problems.forEachIndexed { i, entity -> it["p$i"] = entity.behavior.values.sum() }
		}

		stats.writeToFile("evolution.csv")

		val first = state.solutions.sortedByDescending { it.behavior.values.sum() }.first()

		solutions = state.solutions

		generation = maxGenerations

		finished = true

		progress = 1.0

		return state.solutions.first()
	}

}