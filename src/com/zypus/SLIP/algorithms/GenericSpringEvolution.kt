package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.Entity
import com.zypus.SLIP.algorithms.genetic.EvolutionRules
import com.zypus.SLIP.models.Environment
import com.zypus.SLIP.models.Initial
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.Statistic
import tornadofx.getProperty
import tornadofx.property

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 28/04/16
 */
class GenericSpringEvolution<SG : Any, SP : Any, SB : Any, SBC : Any, PG : Any, PP : Any, PB : Any, PBC : Any>(val initial: Initial, val environment: Environment, val setting: SimulationSetting, val evolutionRule: EvolutionRules<SG, SP, SB, SBC, PG, PP, PB, PBC>, val problemScore: (PBC) -> Double = {0.0}, val solutionScore: (SBC) -> Double): Evolution {

	var solutions by property<List<Entity<*, *, *, *>>>(emptyList())
	override fun solutionsProperty() = getProperty(GenericSpringEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::solutions)

	var problems by property<List<Entity<*, *, *, *>>>(emptyList())
	override fun problemsProperty() = getProperty(GenericSpringEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::problems)

	var generation by property<Int>()
	override fun generationProperty() = getProperty(GenericSpringEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::generation)

	var finished by property(false)
	override fun finishedProperty() = getProperty(GenericSpringEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::finished)

	var progress by property(0.0)
	override fun progressProperty() = getProperty(GenericSpringEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::progress)

	var bestSolution by property<Entity<*,*,*,*>>()
	override fun bestSolutionProperty() = getProperty(GenericSpringEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::bestSolution)

	var bestProblem by property<Entity<*, *, *, *>>()
	override fun bestProblemProperty() = getProperty(GenericSpringEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::bestProblem)

	fun evolve(solutionCount: Int, problemCount: Int, maxGenerations: Int): Entity<SG, SP, SB, SBC> {

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

			problems = state.problems

			generation = g

			state = evolutionRule.selectAndReproduce(state)

			bestSolution = state.solutions.sortedByDescending { solutionScore(it.behaviour!!) }.first()
			bestProblem = state.problems.sortedByDescending { problemScore(it.behaviour!!) }.first()

			progress = g.toDouble() / maxGenerations

			if (Thread.interrupted()) break
		}

		evolutionRule.matchAndEvaluate(state)

		stats?.newRow()?.let {
			it["generation"] = maxGenerations
			state.solutions.forEachIndexed { i, entity -> it["s$i"] = solutionScore(entity.behaviour!!) }
			state.problems.forEachIndexed { i, entity -> it["p$i"] = problemScore(entity.behaviour!!) }
		}

		stats?.writeToFile("evolution3.csv")

		val first = state.solutions.sortedByDescending { solutionScore(it.behaviour!!) }.first()

		solutions = state.solutions

		generation = maxGenerations

		finished = true

		progress = 1.0

		return first
	}

}