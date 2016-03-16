package com.zypus.utilities

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 16/03/16
 */

fun rungeKutta(x: List<Double>, f: List<(List<Double>) -> Double>, h: Double): List<Double> {
	val add: List<Double>.(List<Double>) -> List<Double> = { this.zip(it) { x, y -> x + y } }
	val times: Double.(List<Double>) -> List<Double> = { it.map { it * this } }

	val a = f.map { it(x) }
	val b = f.map { it(x.add(0.5 * h * a)) }
	val c = f.map { it(x.add(0.5 * h * b)) }
	val d = f.map { it(x.add(h * c)) }

	return x.add((h / 6) * a.add(2.0 * b).add(2.0 * c).add(d))
}