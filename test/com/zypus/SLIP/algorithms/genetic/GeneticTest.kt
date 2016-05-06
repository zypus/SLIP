package com.zypus.SLIP.algorithms.genetic

import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.utilities.pickRandom
import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 03/03/16
 */

class GeneticTest {

	@Test
	fun testStringEvolutionWithBuilder() {

		val target = "hello world"
		val alphabet = " abcdefghijklmnopqrstuvwxyz"

		val survivalCount = 10

		val evolutionRules = evolution<Pair<String, String>, String, Int, HashMap<Any, Int>, String, String, Int, HashMap<Any,Int>> {
			solution = {
				// Create a random string of target length.
				initialize = { alphabet.pickRandom(target.length) to  alphabet.pickRandom(target.length)}

				mapping = {
					it.first.zip(it.second).map { p -> if ("${p.first}" < "${p.second}") p.first else p.second }.joinToString(separator = "")
				}

				select = { population ->
					val picked = Selections.elitist(population, survivalCount) {
						(it.behaviour as HashMap<*, *>).values.sumByDouble { it as Double }
					}
					val toBeReplaced = population.filter { e -> !picked.contains(e) }
					Selection(toBeReplaced.size, (0..4).map { picked[2 * it] to picked[2 * it + 1] }, toBeReplaced)
				}

				reproduce = { mother, father ->
					mother.first.crossoverAndMutate(mother.second, alphabet = alphabet, expectedMutations = 1) to
							father.first.crossoverAndMutate(father.second, alphabet = alphabet, expectedMutations = 1)
				}
			}

			singularProblem = target

			test = {
				match = { evolutionState -> evolutionState.solutions.map { it to evolutionState.problems.first() } }

				evaluate = { first, second ->
					val matches = first.zip(second).count { it.first.equals(it.second) }

					matches to matches
				}
			}
		}

		runEvolution2(evolutionRules, target)
	}

	private fun runEvolution(evolution: EvolutionRules<String, String, Int, HashMap<Any,Int>, String, String, Int, HashMap<Any, Int>>, target: String) {
		var state = evolution.initialize(100, 1)

		Assert.assertSame("Solution and problem length are not equal.", target.length, state.solutions.first().phenotype.length)

		println("Initialized:")
		//state.solutions.forEach { println(it.phenotype) }

		var generation = 1

		val solved: (Entity<String, String, Int, HashMap<Any, Int>>) -> Boolean = { e -> (e.behaviour as HashMap<*,*>).values.any { it as Int == target.length } }
		while (state.solutions.none(solved) ) {
			evolution.matchAndEvaluate(state)

			generation++
//			println("Generation ${generation++}:")
//			state.solutions.forEach { println("${it.phenotype} - ${it.behavior.values.first()}") }

			state = evolution.selectAndReproduce(state)

			Assert.assertTrue("To many generations.", generation < 2000)
		}

		val solution = state.solutions.first(solved).phenotype
		println("Final answer is \"$solution\" in $generation generations.")

		Assert.assertTrue("Solution is not equal to target.", target.equals(solution))
	}

	private fun runEvolution2(evolution: EvolutionRules<Pair<String, String>, String, Int, HashMap<Any, Int>, String, String, Int, HashMap<Any, Int>>, target: String) {
		var state = evolution.initialize(100, 1)

		Assert.assertSame("Solution and problem length are not equal.", target.length, state.solutions.first().phenotype.length)

		println("Initialized:")
		//state.solutions.forEach { println(it.phenotype) }

		var generation = 1

		val solved: (Entity<Pair<String,String>, String, Int, HashMap<Any, Int>>) -> Boolean = { e -> (e.behaviour as HashMap<*,*>).values.any { it as Int == target.length } }
		while (state.solutions.none(solved) ) {
			evolution.matchAndEvaluate(state)

			generation++
			//			println("Generation ${generation++}:")
			//			state.solutions.forEach { println("${it.phenotype} - ${it.behavior.values.first()}") }

			state = evolution.selectAndReproduce(state)

			Assert.assertTrue("To many generations. ${state.solutions.first().phenotype}", generation < 5000)
		}

		val solution = state.solutions.first(solved)
		println("Final answer is \"${solution.phenotype}\" in $generation generations by ${solution.genotype}")

		Assert.assertTrue("Solution is not equal to target.", target.equals(solution.phenotype))
	}

}