package com.zypus.math

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

    val norm2 = x*x+y*y

    override fun toString(): String {
        return "( $x , $y )"
    }
}