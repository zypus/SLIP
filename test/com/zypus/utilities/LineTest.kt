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
		val a = LineSegment(Vector2(-1,0), Vector2(1,0))
		val b = LineSegment(Vector2(0, -1), Vector2(0, 1))

		val inter1 = a intersect  b
		val inter2 = b intersect  a

		Assert.assertEquals(Vector2(0, 0), inter1)
		Assert.assertEquals(Vector2(-0.0, -0.0), inter2)
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
			val da = Vector2(0, 1).rotate(r.deg)
			val db = Vector2(1, 0).rotate(r.deg)
			val dc = Vector2(1, 1).rotate(r.deg)

			val a = LineSegment(Vector2(0,0)-da, Vector2(0,0)+da)
			val b = LineSegment(Vector2(0,0)-db, Vector2(0,0)+db)
			val c = LineSegment(Vector2(0,0)-dc, Vector2(0,0)+dc)

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
		val a = LineSegment(Vector2(0, 0), Vector2(1, 0))
		val b = LineSegment(Vector2(0, 0), Vector2(0, 1))

		val inter1 = (a intersect b)!!
		val inter2 = (b intersect a)!!

		Assert.assertTrue(inter1.x == 0.0 && inter1.y == 0.0)
		Assert.assertTrue(inter2.x == 0.0 && inter2.y == 0.0)
	}

	@Test
	fun test_endpoint_intersection() {
		val a = LineSegment(Vector2(1, 0), Vector2(0, 0))
		val b = LineSegment(Vector2(0, 1), Vector2(0, 0))

		val inter1 = (a intersect b)!!
		val inter2 = (b intersect a)!!

		Assert.assertTrue(inter1.x == 0.0 && inter1.y == 0.0)
		Assert.assertTrue(inter2.x == 0.0 && inter2.y == 0.0)
	}

	@Test
	fun test_box() {
		val a = LineSegment(Vector2(0, 0), Vector2(0, 500))
		val b = LineSegment(Vector2(0, 500), Vector2(500, 500))
		val c = LineSegment(Vector2(0, 0), Vector2(500, 0))
		val d = LineSegment(Vector2(500, 0), Vector2(500, 500))

		for (r in 0..3600) {

			val pos = Vector2(250,250)
			val dir = Vector2(0,1).rotate((r/10.0).deg)*10000

			val x = LineSegment(pos, pos+dir)

			val inter1 = x intersect a
			val inter2 = x intersect b
			val inter3 = x intersect c
			val inter4 = x intersect d

			val inters = arrayListOf(inter1, inter2, inter3, inter4)

			Assert.assertNotNull("Null at ${r/10.0}", inters.firstOrNull { it != null })
		}
	}

}