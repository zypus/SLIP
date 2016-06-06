package com.zypus.SLIP.verification

import com.zypus.SLIP.models.SpringController
import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.SLIP.verification.benchmark.ControllerSerializer
import com.zypus.SLIP.verification.benchmark.TerrainSerializer
import java.io.File

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 18/05/16
 */

fun main(args: Array<String>) {
	val algorithms = arrayListOf("terrain diversity", "slip diversity", "pure fitness", "pure diversity")
	val source = arrayListOf("tnc", "snco", "co", "dc")
	val n = 100

	val solutions = source.map {
		val lines = File("results/$it.solutions.sorted.txt").reader().readLines().takeLast(n) as MutableList
		val controllers: MutableList<SpringController> = arrayListOf()
		while (!lines.isEmpty()) {
			val controller = ControllerSerializer.deserialize(lines)
			if (controller != null) {
				controllers.add(controller)
			}
		}
		controllers
	}

	val problems = source.map {
		val lines = File("results/$it.problems.sorted.txt").reader().readLines() as MutableList
		val terrains: MutableList<Terrain> = arrayListOf()
		while (!lines.isEmpty()) {
			val terrain = TerrainSerializer.deserialize(lines)
			if (terrain != null) {
				terrains.add(terrain)
			}
		}
		terrains.take(n)
	}

//	val solutionResults = solutions.map { s ->
//		val map = problems.map { p ->
//			s.mapParallel {
//				Benchmark.evaluate(it, p) {
//					state, off ->
//					state.slip.position.x - off
//				} / p.size
//			}
//		}
//		println("Done")
//		map
//	}
//	val problemResults = problems.map { p ->
//		val map = solutions.map { s ->
//			p.mapParallel {
//				Benchmark.evaluate(it, s) {
//					state, off ->
//					state.slip.position.x - off
//				} / s.size
//			}
//		}
//		println("Done")
//		map
//	}

//	val columns = algorithms.flatMap { s -> algorithms.map { "${s}VS$it" } }
//	run {
//		val statistic = Statistic(*columns.toTypedArray())
//		(0..n - 1).map {
//			with(statistic.newRow()) {
//				columns.mapIndexed { i, s ->
//					this[s] = solutionResults[i / 4][i % 4][it]
//				}
//			}
//		}
//		statistic.writeToFile("results/solutions.average.csv")
//	}
//	run {
//		val statistic = Statistic(*columns.toTypedArray())
//		(0..n - 1).map {
//			with(statistic.newRow()) {
//				columns.mapIndexed { i, s ->
//					this[s] = problemResults[i / 4][i % 4][it]
//				}
//			}
//		}
//		statistic.writeToFile("results/problems.average.csv")
//	}

}