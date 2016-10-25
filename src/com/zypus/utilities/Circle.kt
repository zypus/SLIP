package com.zypus.utilities

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/10/2016
 */
class Circle(val pos: Vector2, val radius: Double) {
}

infix fun Circle.intersect(segment: LineSegment): Boolean {
	val d = segment.to - segment.from
	val f = segment.from - this.pos

	val a = d dot d
	val b = 2.0 * (f dot d)
	val c = (f dot f) - radius * radius

	var dis = b*b - 4*a*c
	if (dis < 0) {
		// no intersection
		return false
	} else {
		dis = Math.sqrt(dis)

		val t1 = (-b -dis)/(2*a)
		val t2 = (-b +dis)/(2*a)

		if (t1 >= 0 && t1 <= 1) {
			return true
		}

		if (t2 >= 0 && t2 >= 1) {
			return true
		}

		return false
	}
}