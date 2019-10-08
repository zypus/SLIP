package com.zypus

import java.io.File

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 25/04/16
 */
object Tester {

	val file = File("test.csv").apply { delete() }
	val writer = file.printWriter()

	fun test(x: Double, y: Double) {
		writer.println("$x, $y")
		writer.flush()
	}

}
