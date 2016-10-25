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
	val (x1, y1) = from
	val (x2, y2) = to
	val (x3, y3) = other.from
	val (x4, y4) = other.to
	val divisor = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
	if (divisor != 0.0) {
		val px = (x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)
		val py = (x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)
		return Vector2(px / divisor, py / divisor)
	}
	else {
		return null
	}
}

data class LineSegment(val from: Vector2, val to: Vector2)

val epsilon = 1E-8

infix fun LineSegment.intersect(other: LineSegment): Vector2? {
	val intersection = Line(from, to) intersect Line(other.from, other.to)
	if (intersection != null) {
		if (
				intersection.x >= min(from.x, to.x) - epsilon &&
				intersection.x <= max(from.x, to.x) + epsilon &&
				intersection.y >= min(from.y, to.y) - epsilon &&
				intersection.y <= max(from.y, to.y) + epsilon &&
				intersection.x >= min(other.from.x, other.to.x) - epsilon &&
				intersection.x <= max(other.from.x, other.to.x) + epsilon &&
				intersection.y >= min(other.from.y, other.to.y) - epsilon &&
				intersection.y <= max(other.from.y, other.to.y) + epsilon
		) {
			return intersection
		}
	}
	return null
}

infix fun LineSegment.intersect(other: Line): Vector2? {
	val intersection = Line(from, to) intersect other
	if (intersection != null) {
		if (
				intersection.x >= min(from.x, to.x) - epsilon &&
				intersection.x <= max(from.x, to.x) + epsilon &&
				intersection.y >= min(from.y, to.y) - epsilon &&
				intersection.y <= max(from.y, to.y) + epsilon ) {
			return intersection
		}
	}
	return null
}