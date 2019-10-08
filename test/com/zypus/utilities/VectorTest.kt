package com.zypus.utilities

import org.junit.Assert
import org.junit.Test

/**
 * TODO Add description

 * @author fabian<zypus@users.noreply.github.com>
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

	@Test
	fun test3() {
		val mazeSize = 50.0
		val gen = arrayListOf(0.0,0.0,10.0,10.0,45.0,45.0)
		val values = arrayListOf(0.2,0.4,0.7)
		val center = mikera.vectorz.Vector2(gen[2], gen[3])
		center.addMultiple(mikera.vectorz.Vector2(gen[4] - gen[2], gen[5] - gen[3]), values[0])
		val dir = mikera.vectorz.Vector2(1.0, 0.0)
		dir.rotate((values[1] * 360).deg)
		val start = center.addMultipleCopy(dir, values[2] * mazeSize)
		dir.negate()
		val end = center.addMultipleCopy(dir, values[2] * mazeSize)
		LineSegment(start as mikera.vectorz.Vector2, end as mikera.vectorz.Vector2)
	}

}
