package com.zypus.SLIP.algorithms.genetic


/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 03/03/16
 */

inline fun <G : Any, P : Any, B : Any> elitist(population: List<Entity<G, P, B>>, count: Int, crossinline fitness: (Entity<G, P, B>) -> Double): List<Entity<G, P, B>> {
	val sortedByFitness = population.sortedByDescending { fitness(it) }
	return sortedByFitness.take(count)
}

inline fun <G: Any, P: Any, B: Any> rouletteWheel(population: List<Entity<G, P, B>>, count: Int, fitness: (Entity<G,P, B>) -> Double): List<Entity<G, P, B>> {

	val sortedByFitness = population.map { it to fitness(it) }.sortedByDescending { it.second }
	val fitnessSum = sortedByFitness.sumByDouble { it.second }
	var normalized = sortedByFitness.map { it.first to it.second/fitnessSum }
	var selected: List<Entity<G,P,B>> = arrayListOf()
	for (i in 1..count) {
		var current = 1.0
		val next = Math.random()
		val lucky = normalized.first {
			val answer = next > current - it.second
			current -= it.second
			answer
		}
		selected += lucky.first
	}
	return selected
}