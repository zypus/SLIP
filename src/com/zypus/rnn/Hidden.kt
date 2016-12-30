package com.zypus.rnn

import mikera.matrixx.Matrix
import mikera.vectorz.Ops
import mikera.vectorz.Vector

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 19/11/2016
 */
class Hidden(ih: Vector, val Wxh: Matrix, val Whh: Matrix, val bh: Vector) {

		// *hidden state
		var h = ih

		fun step(x: Vector): Vector {
			val Wxh_x = Wxh.innerProduct(x)
			h = Whh.innerProduct(h)
			h.add(Wxh_x)
			h.add(bh)
			h.applyOp(Ops.TANH)
			return h
		}
}