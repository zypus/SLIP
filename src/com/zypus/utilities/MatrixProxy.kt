package com.zypus.utilities

import mikera.matrixx.Matrix
import mikera.vectorz.Vector


/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 29/10/2016
 */
data class MatrixProxy(val data: List<Double>, val columns: Int, val rows: Int) {

	fun toMatrix(): Matrix {
		return data.toMatrix(columns, rows)
	}

	fun toVector(): Vector {
		return data.toVector()
	}

}
