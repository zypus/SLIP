package com.zypus.utilities

import golem.end
import golem.mat
import golem.matrix.jblas.JBlasMatrixFactory
import org.junit.Assert
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 12/11/2016
 */
class PerformanceTest {

	@Test
	fun vectorTest() {
		var a = Vector2(1,1)
		val iter = 1000000
		println("time vector2:\t"+measureTimeMillis {
			for (i in 1..iter) {
				a += a
			}
		})

		var b2 = mat[1 end 1]
		println("time jmlt:\t\t" + measureTimeMillis {
			for (i in 1..iter) {
				b2 += b2
			}
		})

		golem.factory = JBlasMatrixFactory()
		var b = mat[1 end 1]
		println("time jblas:\t\t" +measureTimeMillis {
			for (i in 1..iter) {
				b += b
			}
		})

		val c = mikera.vectorz.Vector2(1.0,1.0)
		println("time vectorz:\t" + measureTimeMillis {
			for (i in 1..iter) {
				c.add(c)
			}
		})

		var dx = 1.0
		var dy = 1.0
		println("time pure:\t\t" + measureTimeMillis {
			for (i in 1..iter) {
				dx += dx
				dy += dy
			}
		})

		var e = 1.0 to 1.0
		println("time pair:\t\t" + measureTimeMillis {
			for (i in 1..iter) {
				e = e.first+e.first to e.second+e.second
			}
		})

		Assert.assertTrue(a.x == b[0] && a.y == b[1])
		Assert.assertTrue(b[0] == c.x && b[1] == c.x)
		Assert.assertTrue(c.x == dx && c.y == dy)
		Assert.assertTrue(dx == e.first && dy == e.second)
	}

//	@Test
//	fun rnnTest() {
//		val inputs = 11
//		val hidden = 16
//		val output = 2
//		val Wxh = rand(hidden, inputs)
//		val Whh = rand(hidden, hidden)
//		val Why = rand(output, hidden)
//		val bh = rand(hidden, 1)
//		val by = rand(output, 1)
//		val ih = rand(hidden, 1)
//		val rnn = Rnn(ih, Wxh, Whh, Why, bh, by)
//
//		val iter = 10000
//
//		val x = rand(inputs, 1)
//
//		val w1 = Wxh.toList()
//		val w2 = Whh.toList()
//		val w3 = Why.toList()
//		val b1 = bh.toList()
//		val b2 = by.toList()
//		val i1 = ih.toList()
//		val x1 = x.toList()
//
//		golem.factory = JBlasMatrixFactory()
//
//		var y: golem.matrix.Matrix<Double>? = null
//
//		println("time jbals:\t\t" + measureTimeMillis {
//			for (i in 1..iter) {
//				y = rnn.step(x)
//			}
//		})
//
//		val Wxh2 = Matrix.wrap(hidden, inputs, w1.toDoubleArray())
//		val Whh2 = Matrix.wrap(hidden, hidden, w2.toDoubleArray())
//		val Why2 = Matrix.wrap(output, hidden, w3.toDoubleArray())
//		val bh2 = Vector.create(b1.toDoubleArray())
//		val by2 = Vector.create(b2.toDoubleArray())
//		var ih2 = Vector.create(i1.toDoubleArray())
//
//		val x2 = Vector.create(x1.toDoubleArray())
//
//		var y2: Vector? = null
//
//		println("time matrixx:\t\t" + measureTimeMillis {
//			for (i in 1..iter) {
//				val matrix = Wxh2.innerProduct(x2)
////				println(matrix)
//				val matrix2 = Whh2.innerProduct(ih2)
////				println(matrix2)
//				matrix.add(matrix2)
////				println(matrix)
//				matrix.add(bh2)
////				println(matrix)
//				matrix.applyOp(Ops.TANH)
////				println(matrix)
//				ih2 = matrix
//				// compute the output vector
//				y2 = Why2.innerProduct(ih2)
////				println(y2)
//				y2!!.add(by2)
////				println(y2)
//			}
//		})
//
//		println(y)
//		println(y2)
//
//		Assert.assertTrue(y!![0] == y2!![0] && y!![1] == y2!![1])
//
//	}


}
