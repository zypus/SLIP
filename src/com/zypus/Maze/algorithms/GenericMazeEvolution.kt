package com.zypus.Maze.algorithms

import com.zypus.Maze.models.Maze
import com.zypus.SLIP.algorithms.Evolution
import com.zypus.SLIP.algorithms.genetic.Entity
import com.zypus.SLIP.algorithms.genetic.EvolutionRules
import com.zypus.SLIP.controllers.StatisticDelegate
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
class GenericMazeEvolution<SG : Any, SP : Any, SB : Any, SBC : Any, PG : Any, PP : Any, PB : Any, PBC : Any>(val maze: Maze, val setting: SimulationSetting, val evolutionRule: EvolutionRules<SG, SP, SB, SBC, PG, PP, PB, PBC>, val problemScore: (PBC) -> Double = { 0.0 }, val solutionScore: (SBC) -> Double) : Evolution {

	var solutions by property<List<Entity<*, *, *, *>>>(emptyList())
	override fun solutionsProperty() = getProperty(GenericMazeEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::solutions)

	var problems by property<List<Entity<*, *, *, *>>>(emptyList())
	override fun problemsProperty() = getProperty(GenericMazeEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::problems)

	var generation by property<Int>()
	override fun generationProperty() = getProperty(GenericMazeEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::generation)

	var finished by property(false)
	override fun finishedProperty() = getProperty(GenericMazeEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::finished)

	var progress by property(0.0)
	override fun progressProperty() = getProperty(GenericMazeEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::progress)

	var bestSolution by property<Entity<*, *, *, *>>()
	override fun bestSolutionProperty() = getProperty(GenericMazeEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::bestSolution)

	var bestProblem by property<Entity<*, *, *, *>>()
	override fun bestProblemProperty() = getProperty(GenericMazeEvolution<SG, SP, SB, SBC, PG, PP, PB, PBC>::bestProblem)

	fun evolve(solutionCount: Int, problemCount: Int, maxGenerations: Int, statsDelegate: StatisticDelegate<SG, SP, SB, SBC, PG, PP, PB, PBC>? = null): Entity<SG, SP, SB, SBC> {

		// Setup statistics.
		val stats: Statistic? = statsDelegate?.initialize(solutionCount, problemCount)

		var state = evolutionRule.initialize(solutionCount, problemCount)

		for (g in 0..maxGenerations - 1) {
			evolutionRule.matchAndEvaluate(state)

			if (stats != null) {
				statsDelegate?.update(stats, g, state)
			}

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

		if (stats != null) {
			statsDelegate?.update(stats, maxGenerations, state)
			statsDelegate?.save(stats)
		}

		val first = state.solutions.sortedByDescending { solutionScore(it.behaviour!!) }.first()

		solutions = state.solutions

		generation = maxGenerations

		finished = true

		progress = 1.0

		return first
	}

}