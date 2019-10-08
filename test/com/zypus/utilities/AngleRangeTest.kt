package com.zypus.utilities

import org.junit.Test

/**
 * TODO Add description

 * @author fabian<zypus@users.noreply.github.com>
 * *
 * @created 23/10/2016
 */
class AngleRangeTest {

	@Test
	fun test1() {
		val range = -45.deg..45.deg

		assert(0.deg in range)
		assert(90.deg !in range)
	}

	@Test
	fun test2() {
		val range = 45.deg..-45.deg

		assert(90.deg in range)
		assert(0.deg !in range)
	}

	@Test
	fun test3() {
		val range = 135.deg..-135.deg

		assert(180.deg in range)
		assert(-180.deg in range)
		assert(0.deg !in range)
	}

	@Test
	fun test4() {
		val range = -135.deg..-45.deg

		assert(-90.deg in range)
		assert(0.deg !in range)
	}

}
