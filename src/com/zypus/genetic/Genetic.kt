package com.zypus.SLIP.algorithms.genetic

import com.zypus.utilities.pickRandom

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 01/03/16
 */

data class EvolutionState<SG : Any, SP : Any, SB : Any, PG : Any, PP : Any, PB : Any>(val solutions: List<Entity<SG, SP,SB>>, val problems: List<Entity<PG, PP,PB>>)

data class Entity<G : Any, P : Any, B: Any>(val genotype: G, val behavior: MutableMap<Any, B> = hashMapOf(), val phenoMapping: (G) -> P) {
	val phenotype by lazy { phenoMapping(genotype) }
}

data class Selection<G : Any, P : Any, B : Any>(val parents: List<Pair<Entity<G, P, B>, Entity<G, P, B>>>, val toBeRemoved: List<Entity<G, P, B>>)

interface Evolver<G : Any, P : Any, B : Any> {
	fun phenotypeMapping(genotype: G): P
	fun reproduce(mother: G, father: G): G
	fun select(population: List<Entity<G, P, B>>): Selection<G,P,B>
	fun initialize(): G
}

interface Tester<SG : Any, SP : Any, SB : Any, PG : Any, PP : Any, PB : Any> {
	fun matching(evolutionState: EvolutionState<SG, SP, SB, PG, PP, PB>): List<Pair<Entity<SG, SP, SB>, Entity<PG, PP, PB>>>
	fun evaluation(first: SP, second: PP):Pair<SB, PB>
}

class EvolutionRules<SG : Any, SP : Any, SB : Any, PG : Any, PP : Any, PB : Any>(private val solutionEvolver: Evolver<SG, SP,SB>, private val problemEvolver: Evolver<PG, PP,PB>, private val tester: Tester<SG, SP,SB, PG, PP,PB>) {

	fun initialize(solutionSize: Int, problemSize: Int): EvolutionState<SG, SP, SB, PG, PP, PB> {
		return EvolutionState(
				(1..solutionSize).map { Entity<SG,SP,SB>(genotype = solutionEvolver.initialize(), phenoMapping = { solutionEvolver.phenotypeMapping(it) }) },
				(1..problemSize).map { Entity<PG, PP, PB>(genotype = problemEvolver.initialize(), phenoMapping = { problemEvolver.phenotypeMapping(it) }) }
		)
	}

	fun matchAndEvaluate(evolutionState: EvolutionState<SG, SP, SB, PG, PP, PB>) {
		val matches = tester.matching(evolutionState)
		matches.map { tester.evaluation(it.first.phenotype, it.second.phenotype) }.zip(matches).forEach {
			val (behaviour, match) = it
			match.first.behavior[match.second.genotype] = behaviour.first
			match.second.behavior[match.first.genotype] = behaviour.second
		}
	}

	fun selectAndReproduce(evolutionState: EvolutionState<SG, SP, SB, PG, PP, PB>): EvolutionState<SG, SP, SB, PG, PP, PB> {
		// Remove and reproduce the solution and problem entities.
		val (nextSolutions, removedSolutions) = selectAndReproduce(evolutionState.solutions, solutionEvolver)
		val (nextProblems, removedProblems) = selectAndReproduce(evolutionState.problems, problemEvolver)
		// Clean up the behaviour maps from all entities that were removed.
		nextProblems.forEach { p -> removedSolutions.forEach { p.behavior.remove(it.genotype) } }
		nextSolutions.forEach { s -> removedProblems.forEach { s.behavior.remove(it.genotype) } }
		return EvolutionState(
				nextSolutions,
				nextProblems
		)
	}

	data class SRResult<G : Any, P : Any, B : Any>(val nextGeneration: List<Entity<G,P,B>>, val removed: List<Entity<G, P, B>>)

	private fun <G : Any, P : Any, B: Any> selectAndReproduce(population: List<Entity<G, P, B>>, evolver: Evolver<G, P, B>): SRResult<G,P,B> {
		val (parentCandidates, toBeRemoved) = evolver.select(population)
		val filtered = population.filter { !toBeRemoved.contains(it) }
		val children = toBeRemoved.map {
			val (mother, father) = parentCandidates.pickRandom { Math.random() }
			val childGenotype = evolver.reproduce(mother.genotype, father.genotype)
			Entity<G,P,B>(genotype = childGenotype, phenoMapping = { evolver.phenotypeMapping(it) })
		}

		val nextGeneration = filtered + children
		return SRResult(nextGeneration, toBeRemoved)
	}

}