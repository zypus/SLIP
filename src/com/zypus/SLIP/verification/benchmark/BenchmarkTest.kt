package com.zypus.SLIP.verification.benchmark

import kotlin.system.measureTimeMillis

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 15/05/16
 */

fun main(args: Array<String>) {
	val millis = measureTimeMillis {
		Benchmark.controllerBase.forEach {
			val benchmark = Benchmark.benchmark(it)
			println("Benchmark: $benchmark")
		}
	}
	println("Time past: $millis")

	println("\nTerrains")
	val millis2 = measureTimeMillis {
		Benchmark.terrainBase.forEach {
			val benchmark = Benchmark.benchmark(it)
			println("Benchmark: $benchmark")
		}
	}
	println("Time past: $millis2")
}