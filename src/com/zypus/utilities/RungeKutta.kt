package com.zypus.utilities

import mikera.vectorz.Vector4

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 16/03/16
 */

fun List<Double>.add(other: List<Double>): List<Double> = this.zip(other) { x, y -> x + y }

operator fun Double.times(list: List<Double>): List<Double> = list.map { it * this }

fun rungeKutta(x: List<Double>, f: List<(List<Double>) -> Double>, h: Double): List<Double> {

	val a = f.map { it(x) }
	val b = f.map { it(x.add(0.5 * h * a)) }
	val c = f.map { it(x.add(0.5 * h * b)) }
	val d = f.map { it(x.add(h * c)) }

	return x.add((h / 6) * a.add(2.0 * b).add(2.0 * c).add(d))
}

fun rungeKutta4(x: Vector4, f: List<(Vector4) -> Double>, h: Double): Vector4 {

	val a = Vector4(f[0](x), f[1](x), f[2](x), f[3](x))
	val xx = x.addMultipleCopy(a, 0.5*h) as Vector4
	val b = Vector4(f[0](xx), f[1](xx), f[2](xx), f[3](xx))
	a.addMultiple(b, 2.0)
	xx.set(x)
	xx.addMultiple(b, 0.5*h)
	val c = b
	c.setValues(f[0](xx), f[1](xx), f[2](xx), f[3](xx))
	a.addMultiple(c, 2.0)
	xx.set(x)
	xx.addMultiple(c, h)
	val d = c
	d.setValues(f[0](xx), f[1](xx), f[2](xx), f[3](xx))

	a.add(d)
	a.multiply(h/6)
	a.add(x)

	return a

}


inline fun rungeKutta42(x: Vector4,  crossinline f0: (Vector4) -> Double, crossinline f1: (Vector4) -> Double, crossinline f2: (Vector4) -> Double, crossinline f3: (Vector4) -> Double, h: Double): Vector4 {

	val a = Vector4(f0(x), f1(x), f2(x), f3(x))
	val xx = x.addMultipleCopy(a, 0.5 * h) as Vector4
	val b = Vector4(f0(xx), f1(xx), f2(xx), f3(xx))
	a.addMultiple(b, 2.0)
	xx.set(x)
	xx.addMultiple(b, 0.5 * h)
	val c = b
	c.setValues(f0(xx), f1(xx), f2(xx), f3(xx))
	a.addMultiple(c, 2.0)
	xx.set(x)
	xx.addMultiple(c, h)
	val d = c
	d.setValues(f0(xx), f1(xx), f2(xx), f3(xx))

	a.add(d)
	a.multiply(h / 6)
	a.add(x)

	return a

}
