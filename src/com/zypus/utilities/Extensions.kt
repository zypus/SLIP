package com.zypus.utilities

import mikera.vectorz.Vector2

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 24/02/16
 */

val Number.squared: Double
	get() = Math.pow(toDouble(),2.0)

val Number.cubed: Double
	get() = Math.pow(toDouble(), 3.0)

val Number.percent: Double
	get() = toDouble()/100

fun Number.lerp(other: Number, t:Double): Double = (1-t)*this.toDouble()+t*other.toDouble()

fun List<Boolean>.toDouble(signBit: Boolean = true, exponentBits: Int = 11, mantissaBits: Int = 52, exponentBias: Int = 1023): Double {

	val sign = if (signBit && this[0]) -1 else 1
	val expStart = if (signBit) 1 else 0
	val expEnd = expStart+exponentBits-1
	val fracStart = expEnd+1
	val fracEnd = expEnd+mantissaBits

	println("expStart: $expStart expEnd: $expEnd fracStart: $fracStart fracEnd: $fracEnd")

	val exp = this.slice(expStart..expEnd).foldIndexed(0.0) {i,c,b -> if (b) c + Math.pow(2.0,(i+1).toDouble()) else c}
	val frac = this.slice(fracStart..fracEnd).foldIndexed(1.0) { i, c, b -> if (b) c + Math.pow(2.0, -(i+1).toDouble()) else c }

	println("Sign: $sign Frac: $frac Exp: $exp")

	return sign * frac * Math.pow(2.0, exp-exponentBias)
}

inline operator fun Vector2.component1() = this.x
inline operator fun Vector2.component2() = this.y

infix fun List<Double>.dot(other: List<Double>) = this.zip(other).map { it.first * it.second }.sum()

infix fun Vector2.distanceTo(other: Vector2) = Math.sqrt(Math.pow(this.x-other.x,2.0)+Math.pow(this.y-other.y, 2.0))

infix fun Vector2.angleTo(other: Vector2): Double {
	val a = Math.atan2(y, x)
	val b = Math.atan2(other.y, other.x)
	return normalizeAngle(b - a)
}

fun Vector2.rotate(angle: Angle) {
	val newX = x * Math.cos(angle.rad) - y * Math.sin(angle.rad)
	val newY = x * Math.sin(angle.rad) + y * Math.cos(angle.rad)
	x = newX
	y = newY
}

inline fun normalizeAngle(angle: Double) = (angle % (2 * Math.PI)).let {
	if (it > Math.PI) -Math.PI + (it - Math.PI)
	else if (it < -Math.PI) Math.PI + (it + Math.PI)
	else it
}

