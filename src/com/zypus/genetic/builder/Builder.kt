package com.zypus.SLIP.algorithms.genetic.builder

import com.zypus.SLIP.algorithms.genetic.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 08/03/16
 */

inline fun <SG : Any, SP : Any, SB : Any, PG : Any, PP : Any, PB : Any> evolution(init: EvolutionBuilder<SG, SP, SB, PG, PP, PB>.() -> Unit): EvolutionRules<SG, SP, SB, PG, PP, PB> {
	val builder = EvolutionBuilder<SG, SP, SB, PG, PP, PB>()
	builder.init()
	return builder()
}

class EvolutionBuilder<SG : Any, SP : Any, SB : Any, PG : Any, PP : Any, PB : Any>() {

	var solution: EvolverBuilder<SG,SP,SB>.() -> Unit = {}
	var problem: EvolverBuilder<PG,PP,PB>.() -> Unit = {}
	var test: TesterBuilder<SG, SP, SB, PG, PP, PB>.() -> Unit = {}

	var singularProblem: PG? = null
		set(value) { problem = { initialize = { value!! } } }

	operator fun invoke(): EvolutionRules<SG, SP, SB, PG, PP, PB> {
		val solutionBuilder = EvolverBuilder<SG, SP, SB>()
		solutionBuilder.solution()

		val problemBuilder = EvolverBuilder<PG, PP, PB>()
		problemBuilder.problem()

		val testerBuilder = TesterBuilder<SG, SP, SB, PG, PP, PB>()
		testerBuilder.test()

		return EvolutionRules(solutionBuilder(), problemBuilder(), testerBuilder())
	}

}

class EvolverBuilder<G : Any, P : Any, B : Any>() {
	lateinit var initialize: () -> G
	@Suppress("UNCHECKED_CAST")
	var mapping: (G) -> P = { it as P }
	var select: (List<Entity<G, P, B>>) -> Selection<G, P, B> = { Selection(arrayListOf(), arrayListOf()) }
	var reproduce: (G, G) -> G = { m,f -> m }

	operator fun invoke(): Evolver<G, P, B> {
		return object : Evolver<G, P, B> {
			override fun phenotypeMapping(genotype: G): P {
				return this@EvolverBuilder.mapping(genotype)
			}

			override fun reproduce(mother: G, father: G): G {
				return this@EvolverBuilder.reproduce(mother, father)
			}

			override fun select(population: List<Entity<G, P, B>>): Selection<G, P, B> {
				return this@EvolverBuilder.select(population)
			}

			override fun initialize(): G {
				return this@EvolverBuilder.initialize()
			}
		}
	}
}

class TesterBuilder<SG : Any, SP : Any, SB : Any, PG : Any, PP : Any, PB : Any>() {
	lateinit var match: (EvolutionState<SG, SP, SB, PG, PP, PB>) -> List<Pair<Entity<SG, SP, SB>, Entity<PG, PP, PB>>>
	lateinit var evaluate: (SP, PP) -> Pair<SB, PB>

	operator fun invoke(): Tester<SG, SP, SB, PG, PP, PB> {
		return object : Tester<SG, SP, SB, PG, PP, PB> {
			override fun matching(evolutionState: EvolutionState<SG, SP, SB, PG, PP, PB>): List<Pair<Entity<SG, SP, SB>, Entity<PG, PP, PB>>> {
				return this@TesterBuilder.match(evolutionState)
			}

			override fun evaluation(first: SP, second: PP): Pair<SB, PB> {
				return this@TesterBuilder.evaluate(first,second)
			}

		}
	}
}
