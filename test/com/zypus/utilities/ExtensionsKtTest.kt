package com.zypus.utilities

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

/**
 * TODO Add description

 * @author fabian <zypus@users.noreply.github.com>
 * *
 * @created 10/03/16
 */
class ExtensionsKtTest {

	val ffff = arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
	val OOOO = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
	val _3ff0 = arrayOf(0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0)

	@Test
	fun testToDouble1() {

		val bools = (_3ff0 + OOOO + OOOO + OOOO).map { it == 1 }

		println("Input: $bools")

		val actual = bools.toDouble()
		Assert.assertEquals(1.0, actual, 1e-50)

	}

	@Test
	fun testToDouble2() {

	}

	@Test
	fun testToDouble3() {

	}
}