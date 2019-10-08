package com.zypus.rnn

import mikera.matrixx.Matrix
import mikera.vectorz.Vector

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 19/11/2016
 */
class DeepRnn(val hiddenLayers: List<Hidden>, val Why: Matrix, val by: Vector) {

	fun step(x: Vector): Vector {
		val h = hiddenLayers.fold(x) {
			x, hidden ->
			hidden.step(x)
		}
		val y = Why.innerProduct(h)
		y.add(by)
		return y
	}
}
