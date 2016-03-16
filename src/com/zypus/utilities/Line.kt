package com.zypus.utilities

import java.lang.Math.max
import java.lang.Math.min

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 25/02/16
 */

data class Line(val from: Vector2, val to: Vector2)

infix fun Line.intersect(other: Line): Vector2? {
	val divisor = (from.x - to.x) * (other.from.y-other.to.y) - (from.y - to.y) * (other.from.y - other.to.y)
	if (divisor != 0.0) {
		val px = (from.x*to.y - from.y*to.x) * (other.from.x - other.to.x) - (from.x -to.x) * (other.from.x * other.to.y - other.from.y * other.to.x)
		val py = (from.x * to.y - from.y * to.x) * (other.from.y - other.to.y) - (from.y - to.y) * (other.from.x * other.to.y - other.from.y * other.to.x)
		return Vector2(px/divisor, py/divisor)
	} else {
		return null
	}
}

data class LineSegment(val from: Vector2, val to: Vector2)

infix fun LineSegment.intersect(other: LineSegment): Vector2? {
	val intersection = Line(from, to) intersect Line(other.from, other.to)
	if (intersection != null) {
		if (intersection.x >= min(from.x, to.x) && intersection.x <= max(from.x, to.x)) {
			return intersection
		}
	}
	return null
}