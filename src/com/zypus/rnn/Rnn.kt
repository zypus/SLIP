package com.zypus.rnn

import mikera.matrixx.Matrix
import mikera.vectorz.Ops
import mikera.vectorz.Vector

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 29/10/2016
 */
class Rnn(ih: Vector, val Wxh: Matrix, val Whh: Matrix, val Why: Matrix, val bh: Vector, val `by`: Vector) {

	// *hidden state
	var h = ih

	fun step(x: Vector): Vector {
		val Wxhx = Wxh.innerProduct(x)
		h = Whh.innerProduct(h)
		h.add(Wxhx)
		h.add(bh)
		h.applyOp(Ops.TANH)
		// compute the output vector
		val y = Why.innerProduct(h)
		y.add(by)
		return y
	}

}