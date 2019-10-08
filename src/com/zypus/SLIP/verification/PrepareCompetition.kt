package com.zypus.SLIP.verification

import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.SLIP.verification.benchmark.Benchmark
import com.zypus.SLIP.verification.benchmark.TerrainSerializer
import com.zypus.utilities.mapParallel
import java.io.File

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 18/05/16
 */

fun main(args: Array<String>) {

//	for (filename in arrayListOf("tnc","snco","co","dc")) {
//		val lines = File("results/$filename.solutions.txt").reader().readLines() as MutableList
//		val linesCopy = arrayListOf<String>()
//		linesCopy.addAll(lines)
//		val simulation: MutableList<SpringController> = arrayListOf()
//		while (!lines.isEmpty()) {
//			val controller = ControllerSerializer.deserialize(lines)
//			if (controller != null) {
//				simulation.add(controller)
//			}
//		}
//
//		val benchmark = simulation.mapParallel { Benchmark.benchmark(it) }
//
//		val writer = File("results/$filename.solutions.sorted.txt").printWriter()
//		benchmark.zip(linesCopy).sortedBy {
//			it.first
//		}.forEach {
//			writer.println(it.second)
//		}
//		writer.flush()
//		println("solutions in $filename processed")
//	}
	for (filename in arrayListOf("tnc", "snco", "co", "dc")) {
		val lines = File("results/$filename.problems.txt").reader().readLines() as MutableList
		val terrains: MutableList<Terrain> = arrayListOf()
		while (!lines.isEmpty()) {
			val terrain = TerrainSerializer.deserialize(lines)
			if (terrain != null) {
				terrains.add(terrain)
			}
		}
		val writer = File("results/$filename.problems.sorted.txt").printWriter()

		val benchmark = terrains.mapParallel { Benchmark.benchmark(it) }

		benchmark.zip(terrains).sortedBy {
			it.first
		}.forEach {
			TerrainSerializer.serialize(writer,it.second)
		}
		writer.flush()
		println("problems in $filename processed")
	}
}
