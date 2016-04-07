package com.zypus.SLIP.algorithms.genetic

import com.zypus.SLIP.algorithms.genetic.builder.evolution
import com.zypus.utilities.cubed
import com.zypus.utilities.percent
import com.zypus.utilities.squared
import org.junit.Assert
import org.junit.Test
import java.lang.Math.*
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 07/04/16
 */

class Benchmark {

	@Test
	fun ackley() {
		val evolutionRules = optimisationProblem(arrayListOf(-5.0 to 5.0), 2) {
			val x = it[0]
			val y = it[1]

			-20 * exp(-0.2* sqrt(0.5*(x.squared +y.squared))) - exp(0.5*(cos(2*PI*x)+cos(2*PI*y))) + E + 20
		}
		runEvolution(evolutionRules, 0.0, maxIter = 50000, epsilon = 1E-4)
	}

	@Test
	fun sphere2() {
		val evolutionRules = optimisationProblem(arrayListOf(-5.12 to 5.12), 2) {
			it.sumByDouble { it.squared }
		}
		runEvolution(evolutionRules, 0.0)
	}

	@Test
	fun rosenbrock2() {
		val evolutionRules = optimisationProblem(arrayListOf(-2048.0 to 2048.0), 2) {
			100.0 * (it[1]-it[0].squared).squared + (it[0]-1).squared
		}
		runEvolution(evolutionRules, 0.0, maxIter = 50000)
	}

	@Test
	fun beale() {
		val evolutionRules = optimisationProblem(arrayListOf(-4.5 to 4.5), 2) {
			val x = it[0]
			val y = it[1]

			(1.5 - x + x*y).squared + (2.25 - x + x*y.squared).squared + (2.625 - x + x*y.cubed).squared
		}
		runEvolution(evolutionRules, 0.0, maxIter = 50000, epsilon = 1E-4)
	}

	@Test
	fun goldsteinPrice() {
		val evolutionRules = optimisationProblem(arrayListOf(-2.0 to 2.0), 2) {
			val x = it[0]
			val y = it[1]

			(1 + (x + y + 1).squared * (19 -14*x+3*x.squared - 14*y + 6*x*y + 3*y.squared)) * (30 + (2*x-3*y).squared*(18-32*x+12*x.squared+48*y-36*x*y+27*y.squared))
		}
		runEvolution(evolutionRules, 3.0, maxIter = 50000, epsilon = 1E-4)
	}

	@Test
	fun booth() {
		val evolutionRules = optimisationProblem(arrayListOf(-10.0 to 10.0), 2) {
			val x = it[0]
			val y = it[1]

			(x+2*y-7).squared + (2*x+y-5).squared
		}
		runEvolution(evolutionRules, 0.0, maxIter = 50000, epsilon = 1E-4)
	}

	private fun optimisationProblem(bounds: List<Pair<Double,Double>>, n: Int, f: (List<Double>) -> Double): EvolutionRules<List<Double>, List<Double>, Double, (List<Double>) -> Double, (List<Double>) -> Double, Double> {

		val survivalCount = 10

		return evolution {

			val random = Random()

			val resolveBound = fun (bound: Pair<Double,Double>): Double {
				return if (bound.first == kotlin.Double.NEGATIVE_INFINITY && bound.second == kotlin.Double.POSITIVE_INFINITY) {
					kotlin.Double.MAX_VALUE * random.nextDouble() * if (random.nextBoolean()) 1 else -1
				}
				else {
					bound.second - bound.first * random.nextDouble() + bound.first
				}
			}

			solution = {

				initialize = {
					(1..n).mapIndexed { i, v ->
						// Either use the bound that is provided for the index, or use first bound (assumed to be the bound for all indices)
						val bound = if (bounds.size > i) {
							bounds[i]
						}
						else {
							bounds[0]
						}
						resolveBound(bound)
					}
				}

				// phenotype = genotype, so no explicit mapping is necessary

				select = { population ->
					val picked = elitist(population, survivalCount) {
						it.behavior.values.sumByDouble { it.toDouble() }
					}
					Selection((0..(survivalCount / 2) - 1).map { picked[2 * it] to picked[2 * it + 1] }, population.filter { e -> !picked.contains(e) })
				}

				reproduce = { mother, father ->
					val crossover = mother.crossover(father, 1.0)
					crossover.mutate(1.0) {
						i, e ->
						val bound = if (bounds.size > i) {
							bounds[i]
						} else {
							bounds[0]
						}
						when (random.nextDouble()) {
							// decrease/increase the value a bit
							in 0.0..0.2 -> {
								min(bound.first, e - 0.000001)
							}
							in 0.2..0.4 -> {
								max(e + 0.000001, bound.second)
							}
							// decrease/increase in percent
							in 0.4..0.6 -> {
								min(bound.first, e * 90.percent)
							}
							in 0.6..0.8 -> {
								max(e * 110.percent, bound.second)
							}
							// assign a new random value, which is in the bounds
							else -> {
								resolveBound(bound)
							}
						}
					}.toList()
				}
			}

			singularProblem = f

			test = {
				// all solutions against the problem, no exceptions
				match = { evolutionState -> evolutionState.solutions.map { it to evolutionState.problems.first() } }

				evaluate = {
					solution, problem ->
					val value = -problem.invoke(solution)
					value to value
				}
			}
		}
	}

	// Runs the specified evolutionary algorithm until the minimum is reached or a specified number of generations is reached.
	private fun runEvolution(evolution: EvolutionRules<List<Double>, List<Double>, Double, (List<Double>) -> Double, (List<Double>) -> Double, Double>, minimum: Double, maxIter: Int = 5000, solutionCount: Int = 100, problemCount: Int = 1, epsilon: Double = 1e-6) {
		var state = evolution.initialize(solutionCount, problemCount)

		println("Initialized:")

		var generation = 1

		val solved: (Entity<List<Double>, List<Double>, Double>) -> Boolean = { e -> e.behavior.values.any { abs(-minimum-it) < epsilon  } }
		while (state.solutions.none(solved) ) {
			evolution.matchAndEvaluate(state)

//			println("$generation: ${state.solutions.first().behavior.values.first()}")

			generation++

			state = evolution.selectAndReproduce(state)

			Assert.assertTrue("To many generations($generation). ${state.solutions.first().phenotype} with ${state.solutions.first().behavior.values.first()}", generation < maxIter)
		}

		val solution = state.solutions.first(solved)
		println("Final answer is \"${solution.phenotype}\" in $generation generations by ${solution.genotype} with score of ${solution.behavior.values.first()}")

		Assert.assertTrue("Solution is not equal to target.", solution.behavior.values.any {
			(it - minimum) < epsilon } )
	}

}