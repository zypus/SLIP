package com.zypus.SLIP.algorithms.genetic

import java.util.*


/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 03/03/16
 */

object Selections {

	inline fun <G : Any, P : Any, B : Any, BC : Any> elitist(population: List<Entity<G, P, B, BC>>, count: Int, crossinline fitness: (Entity<G, P, B, BC>) -> Double): List<Entity<G, P, B, BC>> {
		val sortedByFitness = population.sortedByDescending { fitness(it) }
		return sortedByFitness.take(count)
	}

	inline fun <G : Any, P : Any, B : Any, BC : Any> rouletteWheel(population: List<Entity<G, P, B, BC>>, count: Int, crossinline fitness: (Entity<G, P, B, BC>) -> Double): List<Entity<G, P, B, BC>> {

		val sortedByFitness = population.sortedByDescending { fitness(it) }
		val min = fitness(sortedByFitness.last())
		val fitnessSum = sortedByFitness.sumByDouble { fitness(it) } - population.size*min
		var selected: List<Entity<G, P, B, BC>> = arrayListOf()
		for (i in 1..count) {
			var current = 1.0
			val next = Math.random()
			val lucky = sortedByFitness.first {
				p ->
				current -= (fitness(p)-min) / fitnessSum
				next > current
			}
			selected += lucky
		}
		return selected
	}

	fun linear(populationSize: Int, bias: Double, random: Random): Int {
		return (populationSize * (bias - Math.sqrt(bias * bias - 4.0 * (bias - 1) * random.nextDouble())) / 2.0 / (bias - 1)).toInt()
	}

}

fun <T> Collection<T>.linearSelection(bias: Double, random: Random): T {
	return elementAt(Selections.linear(size, bias,random))
}