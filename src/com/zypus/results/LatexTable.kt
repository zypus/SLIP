package com.zypus.results

import java.io.File

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 06/06/16
 */

fun main(args: Array<String>) {
	val fileName = "ris.distance"

	val percent = true
	val ci = false

	val reader = File("$fileName.csv").bufferedReader()

	// Write out header

	reader.readLine()

	val map = hashMapOf<String, MutableMap<String, Pair<String, String>>>()

	reader.forEachLine {
		line ->
		val split = line.split(",")
		val algorithm = map.getOrPut(split[1].trim('"')) { hashMapOf<String, Pair<String, String>>() }
		algorithm.put(split[2].trim('"'),split[3] to split[4])
	}

	val algorithmOrder = arrayListOf("Both Fitness Balanced","Both Fitness","SLIP Diversity","Terrain Diversity","Both Diversity")
	val cycleOrder = arrayListOf("0","800","2000","3200","4000")

	println("\\begin{table}[h]")
	println("\\begin{center}")
	println("\\begin{tabular}{| l || ${cycleOrder.joinToString(separator = " | ") {"c"} } | }")
	println("\\hline \n Algorithm & ${cycleOrder.joinToString(separator = " & ")} \\\\ \n \\hline")

	algorithmOrder.forEach { alg ->
		val row = cycleOrder.joinToString(separator = " & ", postfix = "\\\\") {
			val pair = map[alg]!![it]!!
			"\$ ${"%.2f".format(pair.first.toDouble() * if (percent) 100 else 1)} ${if (ci) " \\pm ${"%.2f".format(pair.second.toDouble() * if (percent) 100 else 1) } " else ""} \$"
		}
		println("${alg.split(" ").map { it.first() }.joinToString(separator = "")} & $row")
	}

	println("\\hline")
	println("\\end{tabular}")
	println("\\end{center}")
	println("\\end{table}")
}
