package com.zypus.utilities

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
class AngleRange(override val start: Angle, override val endInclusive: Angle) : ClosedRange<Angle> {
	override fun contains(value: Angle): Boolean {
		if (start < endInclusive) {
			return start < value && value <= endInclusive
		} else {
			return (endInclusive >= value && start > value) || (endInclusive <= value && start < value)
		}
	}

	fun contains(value: Double): Boolean {
		if (start < endInclusive) {
			return start < value && endInclusive >= value
		}
		else {
			return (endInclusive >= value && start > value) || (endInclusive <= value && start < value)
		}
	}

	override fun toString(): String {
		return "${start.deg}..${endInclusive.deg}"
	}
}

operator fun Angle.rangeTo(other: Angle) = AngleRange(this, other)
