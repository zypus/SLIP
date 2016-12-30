package com.zypus.utilities

import org.junit.Assert
import org.junit.Test

/**
 * TODO Add description

 * @author fabian <zypus@users.noreply.github.com>
 * *
 * @created 23/10/2016
 */
class LineTest {

	@Test
	fun test1() {
		val a = LineSegment(mikera.vectorz.Vector2(-1.0, 0.0), mikera.vectorz.Vector2(1.0, 0.0))
		val b = LineSegment(mikera.vectorz.Vector2(0.0, -1.0), mikera.vectorz.Vector2(0.0, 1.0))

		val inter1 = a intersect b
		val inter2 = b intersect a

		Assert.assertEquals(mikera.vectorz.Vector2(0.0, 0.0), inter1)
		Assert.assertEquals(mikera.vectorz.Vector2(-0.0, -0.0), inter2)
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


		for (r in 0..180) {
			val da = mikera.vectorz.Vector2(0.0, 1.0)
			da.rotate(r.deg)
			val db = mikera.vectorz.Vector2(1.0, 0.0)
			db.rotate(r.deg)
			val dc = mikera.vectorz.Vector2(1.0, 1.0)
			dc.rotate(r.deg)

			val a = LineSegment(mikera.vectorz.Vector2(0.0, 0.0).subCopy(da) as mikera.vectorz.Vector2, da)
			val b = LineSegment(mikera.vectorz.Vector2(0.0, 0.0).subCopy(db) as mikera.vectorz.Vector2, db)
			val c = LineSegment(mikera.vectorz.Vector2(0.0, 0.0).subCopy(dc) as mikera.vectorz.Vector2, dc)

			val inter1 = (a intersect b)!!
			val inter2 = (b intersect a)!!
			val inter3 = (a intersect c)!!

			Assert.assertTrue(inter1.x == 0.0 && inter1.y == 0.0)
			Assert.assertTrue(inter2.x == 0.0 && inter2.y == 0.0)
			Assert.assertTrue(inter3.x == 0.0 && inter3.y == 0.0)
		}
	}

	@Test
	fun test_startpoint_intersection() {
		val a = LineSegment(mikera.vectorz.Vector2(0.0, 0.0), mikera.vectorz.Vector2(1.0, 0.0))
		val b = LineSegment(mikera.vectorz.Vector2(0.0, 0.0), mikera.vectorz.Vector2(0.0, 1.0))

		val inter1 = (a intersect b)!!
		val inter2 = (b intersect a)!!

		Assert.assertTrue(inter1.x == 0.0 && inter1.y == 0.0)
		Assert.assertTrue(inter2.x == 0.0 && inter2.y == 0.0)
	}

	@Test
	fun test_endpoint_intersection() {
		val a = LineSegment(mikera.vectorz.Vector2(1.0, 0.0), mikera.vectorz.Vector2(0.0, 0.0))
		val b = LineSegment(mikera.vectorz.Vector2(0.0, 1.0), mikera.vectorz.Vector2(0.0, 0.0))

		val inter1 = (a intersect b)!!
		val inter2 = (b intersect a)!!

		Assert.assertTrue(inter1.x == 0.0 && inter1.y == 0.0)
		Assert.assertTrue(inter2.x == 0.0 && inter2.y == 0.0)
	}

	@Test
	fun test_box() {
		val a = LineSegment(mikera.vectorz.Vector2(0.0, 0.0), mikera.vectorz.Vector2(0.0, 500.0))
		val b = LineSegment(mikera.vectorz.Vector2(0.0, 500.0), mikera.vectorz.Vector2(500.0, 500.0))
		val c = LineSegment(mikera.vectorz.Vector2(0.0, 0.0), mikera.vectorz.Vector2(500.0, 0.0))
		val d = LineSegment(mikera.vectorz.Vector2(500.0, 0.0), mikera.vectorz.Vector2(500.0, 500.0))

		for (r in 0..3600) {

			val pos = mikera.vectorz.Vector2(250.0, 250.0)
			val dir = mikera.vectorz.Vector2(0.0, 1.0)
			dir.rotate((r / 10.0).deg)
			dir.multiply(10000.0)
			dir.add(pos)

			val x = LineSegment(pos, dir)

			val inter1 = x intersect a
			val inter2 = x intersect b
			val inter3 = x intersect c
			val inter4 = x intersect d

			val inters = arrayListOf(inter1, inter2, inter3, inter4)

			Assert.assertNotNull("Null at ${r / 10.0}", inters.firstOrNull { it != null })
		}
	}

}