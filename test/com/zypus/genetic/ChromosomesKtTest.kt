package com.zypus.genetic

import org.junit.Test

/**
 * TODO Add description

 * @author fabian <zypus@users.noreply.github.com>
 * *
 * @created 20/11/2016
 */
class ChromosomesKtTest {

	@Test
	fun testCrossover() {
		val size = 1000
		val stat: MutableList<Int> = (1..size).map { 0 } as MutableList<Int>
		fun <E> Collection<E>.crossover(other: Collection<E>, expectedCrossovers: Double, normalize: Boolean = true): Collection<E> {

			val max = Math.max(this.size, other.size)
			val chance = if (normalize) expectedCrossovers / max else expectedCrossovers
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

				if (Math.random() < chance) {
					stat[i] += 1
					toggle = !toggle
				}
			}
			return list
		}

		val mother = (1..size).toList()
		val father = (1..size).toList()

		for (i in 1..10000) {
			mother.crossover(father, 0.0)
		}

		stat.forEach(::println)
	}

}
