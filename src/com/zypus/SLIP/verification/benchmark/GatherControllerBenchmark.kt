package com.zypus.SLIP.verification.benchmark

import com.zypus.SLIP.models.SpringController
import com.zypus.utilities.pickRandom
import java.io.File
import java.io.PrintWriter
import java.io.Writer

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 15/05/16
 */

//fun main(args: Array<String>) {
//
//	val initial = Initial()
//	val setting = SimulationSetting()
//	val environment = Environment()
//
//	val file = File("ControllerBenchmark.txt")
//	file.delete()
//	val writer = file.printWriter()
//
//	for (rule in arrayListOf(TerrainNoveltyCoevolution.rule, SLIPNoveltyCoevolution.rule, Coevolution.rule)) {
//		val evolution = GenericSpringEvolution(initial, environment, setting, rule, { if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum() }) {
//			if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum()
//		}
//		EventStreams.valuesOf(evolution.generationProperty()).feedTo {
//			if (it != null && it % 200 == 0) {
//				val entity = evolution.solutionsProperty().get()?.pickRandom() ?: null
//				if (entity != null) {
//					val list = entity.genotype as List<Double>
//					ControllerSerializer.serialize(writer, list)
//					println("Added another controller")
//				}
//
//			}
//		}
//
//		for (r in 1..10) {
//
//			evolution.evolve(50, 50, 2000)
//
//		}
//	}
//	writer.flush()
//
//}

//fun main(args: Array<String>) {
//	val file = File("ApprovedControllerBenchmark.txt")
//	val lines = file.reader().readLines() as MutableList<String>
//
//	val simulation: MutableList<SpringController> = arrayListOf()
//	val lines2 = file.reader().readLines() as MutableList<String>
//	while (!lines2.isEmpty()) {
//		val controller = ControllerSerializer.deserialize(lines2)
//		if (controller != null) {
//			simulation.add(controller)
//		}
//	}
//
//	println("Filtering...")
//
//	var c = 0
//
//	val filtered = lines.zip(simulation).filter {
//		println(c++)
//		Benchmark.benchmark(it.second) != 0.0
//	}.map { it.first }
//
//	val file2 = File("Approved2ControllerBenchmark.txt")
//	val writer = file2.printWriter()
//	filtered.forEach { writer.println(it) }
//	writer.flush()
//}

fun main(args: Array<String>) {
	val file = File("ApprovedControllerBenchmark.txt")
	val lines = file.reader().readLines() as MutableList<String>

	while(lines.size > 25) {
		lines.remove(lines.pickRandom())
	}

	val file2 = File("ShortControllerBenchmark.txt")
	file2.delete()
	val writer = file2.printWriter()
	lines.forEach { writer.println(it) }
	writer.flush()
}

object ControllerSerializer {

	fun serialize(writer: Writer, controller: List<Double>) {
		val printWriter = PrintWriter(writer)
		with(printWriter) {
			println(controller.joinToString())
		}
		printWriter.flush()
	}

	fun deserialize(lines: MutableList<String>): SpringController? {
		if (!lines.isEmpty()) {
			val line = lines.removeAt(0).split(",").map { it.toDouble() }
			return SpringController(
					{ slip -> line[0] * slip.velocity.x + line[1] },
					{ slip -> line[2] * (1.0 - (slip.length / slip.restLength)) + line[3] })
		}
		else {
			return null
		}
	}

}
