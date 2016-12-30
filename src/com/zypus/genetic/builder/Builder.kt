package com.zypus.SLIP.algorithms.genetic.builder

import com.zypus.SLIP.algorithms.genetic.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 08/03/16
 */

inline fun <SG : Any, SP : Any, SB : Any, SBC: Any, PG : Any, PP : Any, PB : Any, PBC: Any> evolution(init: EvolutionBuilder<SG, SP, SB, SBC, PG, PP, PB, PBC>.() -> Unit): EvolutionRules<SG, SP, SB, SBC, PG, PP, PB, PBC> {
	val builder = EvolutionBuilder<SG, SP, SB, SBC, PG, PP, PB, PBC>()
	builder.init()
	return builder()
}

class EvolutionBuilder<SG : Any, SP : Any, SB : Any, SBC : Any, PG : Any, PP : Any, PB : Any, PBC : Any>() {

	var solution: EvolverBuilder<SG, SP, SB,SBC, PG, PP, PB,PBC>.() -> Unit = {}
	var problem: EvolverBuilder<PG, PP, PB,PBC, SG, SP, SB,SBC>.() -> Unit = {}
	var test: TesterBuilder<SG, SP, SB, SBC, PG, PP, PB, PBC>.() -> Unit = {}

	var singularProblem: PG? = null
		set(value) { problem = { initialize = { value!! } } }

	// Take a list of static problems, make sure not to create more problems than contained in the list
	var staticProblems: List<PG>? = null
		set(value) {
			problem = {
				var i = 0
				initialize = {
					value!![i++]
				}
			}
		}

	operator fun invoke(): EvolutionRules<SG, SP, SB, SBC, PG, PP, PB, PBC> {
		val solutionBuilder = EvolverBuilder<SG, SP, SB,SBC, PG, PP, PB,PBC>()
		solutionBuilder.solution()

		val problemBuilder = EvolverBuilder<PG, PP, PB,PBC, SG, SP, SB,SBC>()
		problemBuilder.problem()

		val testerBuilder = TesterBuilder<SG, SP, SB, SBC, PG, PP, PB, PBC>()
		testerBuilder.test()

		return EvolutionRules(solutionBuilder(), problemBuilder(), testerBuilder())
	}

}

class EvolverBuilder<G : Any, P : Any, B : Any, BC: Any, OG : Any, OP : Any, OB : Any, OBC: Any>() {
	lateinit var initialize: () -> G
	@Suppress("UNCHECKED_CAST")
	var mapping: (G) -> P = { it as P }
	var select: (List<Entity<G, P, B, BC>>) -> Selection<G, P, B, BC>? = { null }
	var refine: (List<Entity<G, P, B, BC>>, Int) -> List<Entity<G, P, B, BC>> = { el, n -> el }
	var reproduce: (G, G) -> G = { m,f -> m }
	var behaviour: BehaviourBuilder<G,P,B,BC,OG,OP,OB,OBC>.() -> Unit = {}

	operator fun invoke(): Evolver<G, P, B, BC, OG, OP, OB, OBC> {

		val behaviourBuilder = BehaviourBuilder<G, P, B,BC, OG, OP, OB, OBC>()
		behaviourBuilder.behaviour()

		return object : Evolver<G, P, B, BC, OG, OP, OB, OBC> {
			override fun phenotypeMapping(genotype: G): P {
				return this@EvolverBuilder.mapping(genotype)
			}

			override fun reproduce(mother: G, father: G): G {
				return this@EvolverBuilder.reproduce(mother, father)
			}

			override fun select(population: List<Entity<G, P, B, BC>>): Selection<G, P, B, BC>? {
				return this@EvolverBuilder.select(population)
			}

			override fun refine(population: List<Entity<G, P, B, BC>>, n: Int): List<Entity<G, P, B, BC>> {
				return this@EvolverBuilder.refine(population, n)
			}

			override fun initialize(): G {
				return this@EvolverBuilder.initialize()
			}

			override fun initializeBehaviour(): BC? {
				return behaviourBuilder.initialize()
			}

			override fun storeBehaviour(entity: BC, other: Entity<OG, OP, OB, OBC>, behaviour: B): BC? {
				return behaviourBuilder.store(entity, other, behaviour)
			}

			override fun removeBehaviour(entity: BC, other: Entity<OG, OP, OB, OBC>): BC? {
				return behaviourBuilder.remove(entity, other)
			}
		}
	}
}

class BehaviourBuilder<G : Any, P : Any, B : Any, BC: Any,OG : Any, OP : Any, OB : Any, OBC: Any>() {

	constructor(op: BehaviourBuilder<G, P, B, BC, OG, OP, OB, OBC>.() -> Unit) : this() {
		op()
	}

	var initialize: () -> BC? = { null }
	var store: (BC, Entity<OG, OP, OB, OBC>,B) -> BC? = {e,o,b -> e}
	var remove: (BC, Entity<OG, OP, OB, OBC>) -> BC? = {e,o -> e}
}

class TesterBuilder<SG : Any, SP : Any, SB : Any, SBC : Any, PG : Any, PP : Any, PB : Any, PBC : Any>() {
	lateinit var match: (EvolutionState<SG, SP, SB, SBC, PG, PP, PB, PBC>) -> List<Pair<Entity<SG, SP, SB, SBC>, Entity<PG, PP, PB, PBC>>>
	lateinit var evaluate: (SP, PP) -> Pair<SB, PB>

	operator fun invoke(): Tester<SG, SP, SB, SBC, PG, PP, PB, PBC> {
		return object : Tester<SG, SP, SB, SBC, PG, PP, PB, PBC> {
			override fun matching(evolutionState: EvolutionState<SG, SP, SB, SBC, PG, PP, PB, PBC>): List<Pair<Entity<SG, SP, SB, SBC>, Entity<PG, PP, PB, PBC>>> {
				return this@TesterBuilder.match(evolutionState)
			}

			override fun evaluation(first: SP, second: PP): Pair<SB, PB> {
				return this@TesterBuilder.evaluate(first,second)
			}

		}
	}
}
