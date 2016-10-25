package com.zypus.utilities

@Suppress("UNCHECKED_CAST")
/**
 * Vector implementation.
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 14/02/16
 */

data class Vector2(val x: Double, val y: Double) {

    constructor(x: Number, y: Number): this(x.toDouble(), y.toDouble())

    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(other: Number): Vector2 = Vector2(x * other.toDouble(), y * other.toDouble())
    operator fun div(other: Number): Vector2 = Vector2(x / other.toDouble(), y / other.toDouble())
    infix fun dot(other: Vector2): Double = x * other.x + y * other.y

    infix fun cross(other: Vector2): Double = x * other.y - y * other.x

    fun rotate(angle: Angle) =
            Vector2(
                    x * Math.cos(angle.rad) - y * Math.sin(angle.rad),
                    x * Math.sin(angle.rad) + y * Math.cos(angle.rad)
            )

    val norm2 = x*x+y*y

    val norm = Math.sqrt(norm2)

	fun unit() = this / this.norm

    override fun toString(): String {
        return "( $x , $y )"
    }

	infix fun distanceTo(other: Vector2): Double? {
        return (this - other).norm
	}

	val angle = Math.atan2(y, x)

    infix fun angleTo(other: Vector2): Angle {
		val a = this.angle
		val b = other.angle
        return Angle(b-a)
    }

}