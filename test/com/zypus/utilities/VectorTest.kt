package com.zypus.utilities

import org.junit.Assert
import org.junit.Test

/**
 * TODO Add description

 * @author fabian <zypus@users.noreply.github.com>
 * *
 * @created 23/10/2016
 */
class VectorTest {

	@Test
	fun test1() {
		val a = Vector2(0, 1)
		val b = Vector2(1, 0)

		val ang1 = a angleTo b
		val ang2 = b angleTo a

		Assert.assertEquals(ang1.deg, -90.0, 0.01)
		Assert.assertEquals(ang2.deg, 90.0, 0.01)
	}

	@Test
	fun test2() {
		val a = Vector2(0, 1)
		val b = Vector2(0, -1)

		val ang1 = a angleTo b
		val ang2 = b angleTo a

		Assert.assertEquals(ang1.deg, -180.0, 0.01)
		Assert.assertEquals(ang2.deg, 180.0, 0.01)
	}

}