package com.zypus.utilities

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
class Angle(rad: Double): Comparable<Angle> {

	val rad = (rad % (2*Math.PI)).let {
		if (it > Math.PI) -Math.PI+(it - Math.PI)
		else if (it < -Math.PI) Math.PI+(it + Math.PI)
		else it
	}

	override fun compareTo(other: Angle): Int {
		return this.rad.compareTo(other.rad)
	}

	operator fun plus(other: Angle): Angle = Angle(rad + other.rad)

	operator fun minus(other: Angle): Angle = Angle(rad - other.rad)

	operator fun unaryMinus() = Angle(-rad)
	val deg: Double
		get() = Math.toDegrees(rad)
}

val Number.deg: Angle
	get() = Angle(Math.toRadians(this.toDouble()))