package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.*
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.SinusTerrain
import javafx.beans.property.ObjectProperty
import tornadofx.getProperty
import tornadofx.property
import java.util.*

class SpringEvolution3(val initial: Initial, val environment: Environment, val setting: SimulationSetting): Evolution {

	var solutions by property<List<Entity<*, *, *,*>>>(emptyList())
	override fun solutionsProperty() = getProperty(SpringEvolution2::solutions)

	var problems by property<List<Entity<*, *, *, *>>>(emptyList())
	override fun problemsProperty() = getProperty(SpringEvolution2::problems)

	var generation by property<Int>()
	override fun generationProperty() = getProperty(SpringEvolution2::generation)

	var finished by property(false)
	override fun finishedProperty() = getProperty(SpringEvolution2::finished)

	var progress by property(0.0)
	override fun progressProperty() = getProperty(SpringEvolution2::progress)

	override fun bestSolutionProperty(): ObjectProperty<Entity<*, *, *, *>> {
		throw UnsupportedOperationException()
	}

	override fun bestProblemProperty(): ObjectProperty<Entity<*, *, *, *>> {
		throw UnsupportedOperationException()
	}

	val evolutionRule = evolution<List<Double>, SpringController, Double, HashMap<Any,Double>, List<Double>, Environment, Double, HashMap<Any, Double>> {

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

		/* MARK: Solution */

		// Model of the spring controller: controller controls the angle of the spring while in flight phase and controls the spring constant in stance phase.
		solution = {

			initialize = {
				(1..4).mapIndexed { i, v ->
					// Either use the bound that is provided for the index, or use first bound (assumed to be the bound for all indices)
					resolveBound(solutionBounds.getOrElse(i){solutionBounds[0]})
				}
			}

			mapping = { gen -> SpringController ({ slip -> gen[0] * slip.velocity.x + gen[1]},{ slip -> gen[2] * (1.0-(slip.length/slip.restLength)) + gen[3] }) }

			select = { population ->
				val picked = Selections.elitist(population, 10) {
					it.behaviour?.values?.sum() ?: 0.0
				}
				val toBeReplaced = population.filter { e -> !picked.contains(e) }
				Selection(toBeReplaced.size, (0..4).map { picked[2 * it] to picked[2 * it + 1] }, toBeReplaced)
			}

			reproduce = { mother, father ->
				val crossover = mother.crossover(father, 1.0)
				crossover.mutate(1.0) {
					i, e ->
					val bound = if (solutionBounds.size > i) {
						solutionBounds[i]
					}
					else {
						solutionBounds[0]
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
					// assign a new random value, which is in the solutionBounds
						else        -> {
							resolveBound(bound)
						}
					}
				}.toList()
			}

			behaviour = {
				initialize = { hashMapOf() }
				store = { e, o, b -> e[o.genotype] = b; e }
				remove = { e, o -> e.remove(o.genotype); e }
			}

		}

		//		singularProblem = environment

		/* MARK: Problem */

		val problemBounds = arrayListOf(0.001 to 0.3)

		problem = {

			initialize = {
				(1..1).mapIndexed { i, v ->
					// Either use the bound that is provided for the index, or use first bound (assumed to be the bound for all indices)
					resolveBound(solutionBounds.getOrElse(i) { problemBounds[0] })
				}}

			mapping = { gen -> Environment(terrain = SinusTerrain(0.3, 10.0, 0.0, 30.0)) }

			behaviour = {
				initialize = { hashMapOf() }
				store = { e, o, b -> e[o.genotype] = b; e }
				remove = { e, o -> e.remove(o.genotype); e }
			}

		}

		/* MARK: Evaluation */

		test = {

			match = {
				evolutionState ->
				val flatMap = evolutionState.solutions.flatMap { s -> evolutionState.problems.map { s to it } }
				flatMap
			}

			evaluate = {
				controller, environment ->
				var state = SimulationState(SLIP(initial).copy(controller = controller), environment)
				for (i in 1..2000) {
					state = SimulationController.step(state, setting)
					if (state.slip.crashed) break
				}
				val x = state.slip.position.x
				x to x
			}

		}

	}

	fun evolve(): Entity<List<Double>, SpringController, Double, HashMap<Any, Double>> {
		// Population settings.
		val solutionCount = 100
		val problemCount = 1

		// Evolution settings.
		val maxGenerations = 100

		// Setup statistics.
		var columns: MutableList<String> = arrayListOf("generation")
		repeat(solutionCount) { columns.add("s$it") }
		repeat(problemCount) { columns.add("p$it") }
		val stats: Statistic? = null//Statistic(*columns.toTypedArray())


		var state = evolutionRule.initialize(solutionCount, problemCount)

		for (g in 0..maxGenerations - 1) {
			evolutionRule.matchAndEvaluate(state)

//			stats?.newRow()?.let {
//				it["generation"] = g
//				state.solutions.forEachIndexed { i, entity -> it["s$i"] = entity.behavior.values.sum() }
//				state.problems.forEachIndexed { i, entity -> it["p$i"] = entity.behavior.values.sum() }
//			}

			solutions = state.solutions

			generation = g

			state = evolutionRule.selectAndReproduce(state)

			progress = g.toDouble()/maxGenerations

			if (Thread.interrupted()) break
		}

		evolutionRule.matchAndEvaluate(state)

		stats?.newRow()?.let {
			it["generation"] = maxGenerations
			state.solutions.forEachIndexed { i, entity -> it["s$i"] = (entity.behaviour as HashMap<*, *>).values.sumByDouble { it as Double } }
			state.problems.forEachIndexed { i, entity -> it["p$i"] = (entity.behaviour as HashMap<*, *>).values.sumByDouble { it as Double } }
		}

		stats?.writeToFile("evolution3.csv")

		val first = state.solutions.sortedByDescending { (it.behaviour as HashMap<*, *>).values.sumByDouble { it as Double } }.first()

		solutions = state.solutions

		generation = maxGenerations

		finished = true

		progress = 1.0

		return first
	}

}
