package com.zypus.SLIP.algorithms.genetic

import com.zypus.math.pickRandom
import sun.jvm.hotspot.debugger.posix.elf.ELFSectionHeader

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 03/03/16
 */

fun String.crossoverAndMutate(other: String, expectedCrossovers: Int = 1, expectedMutations: Int = 0, alphabet: String = "ACGT"): String {
	val max = Math.max(this.length, other.length)
	val chance: Double = expectedCrossovers.toDouble() / max
	val mutability: Double = expectedMutations.toDouble() / max
	var toggle = Math.random() > 0.5
	var cross = ""
	for (i in 0..max - 1) {
		val next = if (toggle) {
			this.elementAtOrNull(i) ?: other[i]
		}
		else {
			other.elementAtOrNull(i) ?: this[i]
		}
		// Check if the current position gets mutated.
		if (Math.random() < mutability) {
			cross += alphabet.filter { it != next }.pickRandom()
		}
		else {
			cross += next
		}
		if (Math.random() < chance) toggle = !toggle
	}
	return cross
}

fun <E> Collection<E>.crossover(other: Collection<E>, expectedCrossovers: Double, normalize: Boolean = true): Collection<E> {

	val max = Math.max(this.size, other.size)
	val chance = if (normalize) expectedCrossovers/max else expectedCrossovers
	var toggle = Math.random() > 0.5

	val list: MutableList<E> = arrayListOf()

	for (i in 0..max - 1) {
		val next = if (toggle) {
			this.elementAtOrNull(i) ?: other.elementAt(i)
		}
		else {
			other.elementAtOrNull(i) ?: this.elementAt(i)
		}

		list.add(next)

		if (Math.random() < chance) toggle = !toggle
	}
	return list
}

fun <E> Collection<E>.mutate(expectedMutations: Double, normalize: Boolean = true, mutation: (E) -> E): Collection<E> {

	val chance = if (normalize) expectedMutations / this.size else expectedMutations

	return this.map { if (Math.random() < chance) mutation(it) else it }

}

fun Long.crossover(other: Long, expectedCrossovers: Double, normalize: Boolean = true): Long {
	val chance = if (normalize) expectedCrossovers / 64 else expectedCrossovers
	var toggle = Math.random() > 0.5

	var mask = 1L

	var child = 0L

	for (i in 0..64 - 1) {
		val next = if (toggle) {
			child = child or (this and mask)
		}
		else {
			child = child or (other and mask)
		}

		mask = mask shl 1

		if (Math.random() < chance) toggle = !toggle
	}

	return child
}

fun Long.mutate(expectedMutations: Double, normalize: Boolean = true): Long {

	val chance = if (normalize) expectedMutations / 64 else expectedMutations

	var mutated = 0L

	var mask = 1L

	for (i in 0..64 - 1) {

		mutated = mutated or (this and mask)

		if (chance < Math.random()) {
			mutated = mutated xor mask
		}

		mask = mask shl 1
	}

	return mutated
}