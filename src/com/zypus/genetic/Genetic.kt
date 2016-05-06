package com.zypus.SLIP.algorithms.genetic

import com.zypus.utilities.pickRandom

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 01/03/16
 */

data class EvolutionState<SG : Any, SP : Any, SB : Any, SBC: Any, PG : Any, PP : Any, PB : Any, PBC: Any>(val solutions: List<Entity<SG, SP,SB, SBC>>, val problems: List<Entity<PG, PP,PB, PBC>>)

data class Entity<G : Any, P : Any, B: Any, BC: Any>(val genotype: G, var behaviour: BC? = null, val phenoMapping: (G) -> P) {
	val phenotype by lazy { phenoMapping(genotype) }
}

data class Selection<G : Any, P : Any, B : Any, BC: Any>(val childCount: Int, val parents: List<Pair<Entity<G, P, B, BC>, Entity<G, P, B, BC>>>, val toBeRemoved: List<Entity<G, P, B, BC>> = arrayListOf())

interface Evolver<G : Any, P : Any, B : Any, BC: Any, OG : Any, OP : Any, OB : Any, OBC: Any> {
	fun phenotypeMapping(genotype: G): P
	fun reproduce(mother: G, father: G): G
	fun select(population: List<Entity<G, P, B, BC>>): Selection<G,P,B, BC>?
	fun refine(population: List<Entity<G, P, B, BC>>, n: Int): List<Entity<G,P,B,BC>>
	fun initialize(): G
	fun initializeBehaviour(): BC?
	fun storeBehaviour(entity: BC, other: Entity<OG, OP, OB, OBC>, behaviour: B): BC?
	fun removeBehaviour(entity: BC, other: Entity<OG, OP, OB, OBC>): BC?
}

interface Tester<SG : Any, SP : Any, SB : Any, SBC : Any, PG : Any, PP : Any, PB : Any, PBC : Any> {
	fun matching(evolutionState: EvolutionState<SG, SP, SB, SBC, PG, PP, PB, PBC>): List<Pair<Entity<SG, SP, SB, SBC>, Entity<PG, PP, PB, PBC>>>
	fun evaluation(first: SP, second: PP):Pair<SB, PB>
}

class EvolutionRules<SG : Any, SP : Any, SB : Any, SBC : Any, PG : Any, PP : Any, PB : Any, PBC : Any>(private val solutionEvolver: Evolver<SG, SP,SB, SBC, PG, PP, PB, PBC>, private val problemEvolver: Evolver<PG, PP,PB, PBC, SG, SP, SB, SBC>, private val tester: Tester<SG, SP,SB, SBC, PG, PP,PB, PBC>) {

	var solutionPopulationSize = 0
	var problemPopulationSize = 0

	fun initialize(solutionSize: Int, problemSize: Int): EvolutionState<SG, SP, SB, SBC, PG, PP, PB, PBC> {
		solutionPopulationSize = solutionSize
		problemPopulationSize = problemSize
		return EvolutionState(
				(1..solutionSize).map { Entity<SG,SP,SB, SBC>(genotype = solutionEvolver.initialize(), behaviour = solutionEvolver.initializeBehaviour() ,phenoMapping = { solutionEvolver.phenotypeMapping(it) }) },
				(1..problemSize).map { Entity<PG,PP,PB, PBC>(genotype = problemEvolver.initialize(), behaviour = problemEvolver.initializeBehaviour(),phenoMapping = { problemEvolver.phenotypeMapping(it) }) }
		)
	}

	fun matchAndEvaluate(evolutionState: EvolutionState<SG, SP, SB, SBC, PG, PP, PB, PBC>) {
		val matches = tester.matching(evolutionState)
		val processors = Runtime.getRuntime().availableProcessors()
		val subtaskSize = matches.size/processors
		val results = Array(processors) {
			listOf<Pair<Pair<SB,PB>,Pair<Entity<SG,SP,SB,SBC>,Entity<PG,PP,PB,PBC>>>>()
		}
		(1..processors).map {
			val subtask = if (it == processors) {
				matches.subList((it-1)*subtaskSize, matches.size)
			}
			else {
				matches.subList((it-1)*subtaskSize, it*subtaskSize)
			}
			val task = Thread {
				results[it-1] = subtask.map { tester.evaluation(it.first.phenotype, it.second.phenotype) }.zip(subtask)
			}
			task.start()
			task
		}.forEach {
			try {
				it.join()
			} catch (e: InterruptedException) {
				Thread.currentThread().interrupt()
				return
			}
		}
		results.flatMap { it }.forEach {
			val (behaviour, match) = it
			val (solutionBehaviour, problemBehaviour) = behaviour
			val (solution, problem) = match
			solution.behaviour = solutionEvolver.storeBehaviour(solution.behaviour!!, problem, solutionBehaviour)
			problem.behaviour = problemEvolver.storeBehaviour(problem.behaviour!!, solution, problemBehaviour)
		}
	}

	fun selectAndReproduce(evolutionState: EvolutionState<SG, SP, SB, SBC, PG, PP, PB, PBC>): EvolutionState<SG, SP, SB, SBC, PG, PP, PB, PBC> {
		// Remove before selection.
		val refinedSolutions = solutionEvolver.refine(evolutionState.solutions, solutionPopulationSize)
		val refinedProblems = problemEvolver.refine(evolutionState.problems, problemPopulationSize)
		// Remove and reproduce the solution and problem entities.
		val (nextSolutions, removedSolutions) = selectAndReproduce(refinedSolutions, solutionEvolver)
		val (nextProblems, removedProblems) = selectAndReproduce(refinedProblems, problemEvolver)
		// Clean up the behaviour maps from all entities that were removed.
		nextProblems.forEach { p -> removedSolutions.forEach { p.behaviour = problemEvolver.removeBehaviour(p.behaviour!!,it) } }
		nextSolutions.forEach { s -> removedProblems.forEach { s.behaviour = solutionEvolver.removeBehaviour(s.behaviour!!, it) } }
		return EvolutionState(
				nextSolutions,
				nextProblems
		)
	}

	data class SRResult<G : Any, P : Any, B : Any, BC: Any>(val nextGeneration: List<Entity<G,P,B, BC>>, val removed: List<Entity<G, P, B, BC>>)

	private fun <G : Any, P : Any, B: Any, BC: Any, OG : Any, OP : Any, OB : Any, OBC: Any> selectAndReproduce(population: List<Entity<G, P, B, BC>>, evolver: Evolver<G, P, B, BC, OG, OP, OB, OBC>): SRResult<G,P,B,BC> {
		val selection = evolver.select(population)
		if (selection != null) {
			val (count, parentCandidates, toBeRemoved) = selection
			val filtered = population.filter { !toBeRemoved.contains(it) }
			val children = (1..count).map {
				val (mother, father) = parentCandidates.pickRandom { Math.random() }
				val childGenotype = evolver.reproduce(mother.genotype, father.genotype)
				Entity<G, P, B, BC>(genotype = childGenotype, behaviour = evolver.initializeBehaviour(), phenoMapping = { evolver.phenotypeMapping(it) })
			}

			val nextGeneration = filtered + children
			return SRResult(nextGeneration, toBeRemoved)
		} else {
			return SRResult(population, arrayListOf())
		}
	}

}