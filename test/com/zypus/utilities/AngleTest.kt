package com.zypus.utilities

import org.junit.Assert
import org.junit.Test

/**
 * TODO Add description

 * @author fabian<zypus@users.noreply.github.com>
 * *
 * @created 23/10/2016
 */
class AngleTest {

	@Test
	fun test1() {
		val ang1 = 0.deg
		val ang2 = 90.deg

		Assert.assertEquals((ang1 + ang2).deg, 90.0, 0.0001)
	}

	@Test
	fun test2() {
		val ang1 = 90.deg
		val ang2 = 90.deg

		Assert.assertEquals(180.0, (ang1 + ang2).deg, 0.0001)
	}

	@Test
	fun test3() {
		val ang1 = 90.deg
		val ang2 = 91.deg

		Assert.assertEquals(-179.0,(ang1 + ang2).deg, 0.0001)
	}

	@Test
	fun test4() {
		val ang1 = -90.deg
		val ang2 = -91.deg

		Assert.assertEquals(179.0, (ang1 + ang2).deg, 0.0001)
	}

}
