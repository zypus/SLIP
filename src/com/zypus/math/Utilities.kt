package com.zypus.math

import java.lang.Math.sqrt

/**
 * Math related utility functions and classes.
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 17/02/16
 */

fun midnight(a: Double, b: Double, c: Double): Pair<Double?, Double?> {
    // Compute the discriminant.
    val d = b * b - 4 * a * c
    // If discriminant is less than zero, no real solution exits.
    if (d < 0) return null to null
    else {
        val r1 = 0.5 * (-b - sqrt(d)) / a
        if (d == 0.0) return r1 to null
        else return r1 to (0.5 * (-b + sqrt(d)) / a)
    }
}

fun <E : Any?> Collection<E>.pickRandom(): E {
    return pickRandom(Math::random)
}

inline fun <E: Any?> Collection<E>.pickRandom(crossinline random: () -> Double): E {
    return this.elementAt(Math.floor(random()*(this.count()-1)).toInt())
}

fun String.pickRandom(count: Int = 1): String {
    return pickRandom(count, Math::random)
}

inline fun String.pickRandom(count: Int = 1, crossinline random: () -> Double): String {
    return (1..count).map { this[Math.round((length - 1) * random()).toInt()] }.joinToString(separator = "")
}