package com.zypus.utilities

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 24/02/16
 */

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
