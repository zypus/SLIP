package com.zypus.genetic.builder

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 30/10/2016
 */
class EvolutionRule {

	fun population() {

	}

}

fun evolver(init: EvolutionRule.() -> Unit ): EvolutionRule {
	val rule = EvolutionRule()
	rule.init()
	return rule
}

class EvolutionPopulation {

}
