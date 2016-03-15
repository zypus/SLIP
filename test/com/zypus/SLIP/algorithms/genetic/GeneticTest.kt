package com.zypus.SLIP.algorithms.genetic

import com.zypus.math.pickRandom
import com.zypus.SLIP.algorithms.genetic.builder.evolution
import org.junit.Assert
import org.junit.Test

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 03/03/16
 */

class GeneticTest {

	@Test
	fun testStringEvolution() {

		val target = "hello world"
		val alphabet = " abcdefghijklmnopqrstuvwxyz"

		val solutionEvolver: Evolver<String, String, Int> = object : Evolver<String, String, Int> {
			override fun phenotypeMapping(genotype: String): String {
				return genotype
			}

			override fun reproduce(mother: String, father: String): String {
				return mother.crossoverAndMutate(father, alphabet = alphabet, expectedMutations = 3)
			}

			override fun select(population: List<Entity<String, String, Int>>): Selection<String, String, Int> {
				val picked = elitist(population, 10) {
					it.behavior.values.sumByDouble { it.toDouble() }
				}
				return Selection((0..4).map { picked[2 * it] to picked[2 * it + 1] }, population.filter { !picked.contains(it) })
			}

			override fun initialize(): String {
				return alphabet.pickRandom(target.length)
			}

		}

		val problemEvolver: Evolver<String, String, Int> = object : Evolver<String, String, Int> {
			override fun phenotypeMapping(genotype: String): String {
				return genotype
			}

			override fun reproduce(mother: String, father: String): String {
				throw UnsupportedOperationException()
			}

			override fun select(population: List<Entity<String, String, Int>>): Selection<String, String, Int> {
				return Selection(arrayListOf(), arrayListOf())
			}

			override fun initialize(): String {
				return target
			}
		}

		val tester: Tester<String, String, Int, String, String, Int> = object : Tester<String, String, Int, String, String, Int> {
			override fun matching(evolutionState: EvolutionState<String, String, Int, String, String, Int>): List<Pair<Entity<String, String, Int>, Entity<String, String, Int>>> {

				return evolutionState.solutions.map { it to evolutionState.problems.first() }
			}

			override fun evaluation(first: String, second: String): Pair<Int, Int> {

				val matches = first.zip(second).count { it.first.equals(it.second) }

				return matches to matches
			}
		}

		val evolution = EvolutionRules(solutionEvolver, problemEvolver, tester)

		runEvolution(evolution, target)
	}

	@Test
	fun testStringEvolutionWithBuilder() {

		val target = "hello world"
		val alphabet = " abcdefghijklmnopqrstuvwxyz"

		val survivalCount = 10

		val evolutionRules = evolution<Pair<String, String>, String, Int, String, String, Int> {
			solution = {
				// Create a random string of target length.
				initialize = { alphabet.pickRandom(target.length) to  alphabet.pickRandom(target.length)}

				mapping = {
					it.first.zip(it.second).map { p -> if ("${p.first}" < "${p.second}") p.first else p.second }.joinToString(separator = "")
				}

				select = { population ->
					val picked = elitist(population, survivalCount) {
						it.behavior.values.sumByDouble { it.toDouble() }
					}
					Selection((0..(survivalCount/2)-1).map { picked[2 * it] to picked[2 * it + 1] }, population.filter { e -> !picked.contains(e) })
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

	private fun runEvolution(evolution: EvolutionRules<String, String, Int, String, String, Int>, target: String) {
		var state = evolution.initialize(100, 1)

		Assert.assertSame("Solution and problem length are not equal.", target.length, state.solutions.first().phenotype.length)

		println("Initialized:")
		//state.solutions.forEach { println(it.phenotype) }

		var generation = 1

		val solved: (Entity<String, String, Int>) -> Boolean = { e -> e.behavior.values.any { it == target.length } }
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

	private fun runEvolution2(evolution: EvolutionRules<Pair<String, String>, String, Int, String, String, Int>, target: String) {
		var state = evolution.initialize(100, 1)

		Assert.assertSame("Solution and problem length are not equal.", target.length, state.solutions.first().phenotype.length)

		println("Initialized:")
		//state.solutions.forEach { println(it.phenotype) }

		var generation = 1

		val solved: (Entity<Pair<String,String>, String, Int>) -> Boolean = { e -> e.behavior.values.any { it == target.length } }
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