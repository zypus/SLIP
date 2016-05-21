package com.zypus.SLIP.verification

import com.zypus.SLIP.algorithms.*
import com.zypus.SLIP.algorithms.genetic.EvolutionState
import com.zypus.SLIP.controllers.StatisticDelegate
import com.zypus.SLIP.models.Environment
import com.zypus.SLIP.models.SpringController
import com.zypus.SLIP.models.Statistic
import com.zypus.SLIP.verification.benchmark.Benchmark
import com.zypus.SLIP.verification.benchmark.ControllerSerializer
import com.zypus.SLIP.verification.benchmark.TerrainSerializer
import org.reactfx.EventStreams
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 09/05/16
 */

fun main(args: Array<String>) {
	val initial = Coevolution.initial
	val setting = Coevolution.setting
	val environment = Environment()

	var totalTime = 0L;
	var passedRuns = 0;
	val runs = 20;

	val times: MutableList<Long> = arrayListOf()

	val algorithms = mapOf("tnc6." to TerrainNoveltyCoevolution.rule, "snc6." to SLIPNoveltyCoevolution.rule, "c6." to Coevolution.rule, "dc6." to DiversityCoevolution.rule)
	for ((filename, rule) in algorithms) {
		val solutionWriter = File("results/${filename}solutions.txt").printWriter()
		val problemWriter = File("results/${filename}problems.txt").printWriter()
		val evolution = GenericSpringEvolution(initial, environment, setting, rule, { if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum() }) {
			if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum()
		}
		EventStreams.valuesOf(evolution.progressProperty()).feedTo {
			println("%03.1f %%".format(it * 100))
			if (it == 1.0) {
				evolution.solutionsProperty().get().forEach { ControllerSerializer.serialize(solutionWriter, it.genotype as List<Double>) }
				evolution.problemsProperty().get().forEach { TerrainSerializer.serialize(problemWriter, (it.phenotype as Environment).terrain) }
			}
		}

		for (r in 1..runs) {

			println("Beginning run $r")

			val time = measureTimeMillis {
				evolution.evolve(50, 50, 1000, object : StatisticDelegate<List<Double>, SpringController, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>> {
					override fun initialize(solutionCount: Int, problemCount: Int): Statistic {
						val columns: MutableList<String> = arrayListOf("generation")
						repeat(solutionCount + 1) {
							columns.add("s$it fitness")
							columns.add("s$it a")
							columns.add("s$it b")
							columns.add("s$it c")
							columns.add("s$it d")
							columns.add("s$it benchmark")
						}
						repeat(problemCount + 1) {
							columns.add("p$it fitness")
							columns.add("p$it height")
							columns.add("p$it spikiness")
							columns.add("p$it ascension")
							columns.add("p$it benchmark")
						}
						return Statistic(*columns.toTypedArray())
					}

					override fun update(row: Statistic.Row, generation: Int, state: EvolutionState<List<Double>, SpringController, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>>) {
						row["generation"] = generation
						state.solutions.forEachIndexed { i, entity ->
							row["s$i fitness"] = entity.behaviour!!.sum()
							row["s$i a"] = entity.genotype[0]
							row["s$i b"] = entity.genotype[1]
							row["s$i c"] = entity.genotype[2]
							row["s$i d"] = entity.genotype[3]
						}
						state.problems.forEachIndexed { i, entity ->
							row["p$i fitness"] = entity.behaviour!!.sum()
							row["p$i height"] = TerrainDifficulty.meanHeight(entity.phenotype.terrain)
							row["p$i spikiness"] = TerrainDifficulty.spikiness(entity.phenotype.terrain)
							row["p$i ascension"] = TerrainDifficulty.ascension(entity.phenotype.terrain)
						}
						if (generation % 100 == 0) {
							println("Benchmarking generation $generation ...")
							/* Benchmark solutions */
							run {
								val processors = Runtime.getRuntime().availableProcessors()
								val subtaskSize = state.solutions.size / processors
								val results = Array(processors) {
									listOf<Double>()
								}
								(1..processors).map {
									val subtask = if (it == processors) {
										state.solutions.subList((it - 1) * subtaskSize, state.solutions.size)
									}
									else {
										state.solutions.subList((it - 1) * subtaskSize, it * subtaskSize)
									}
									val task = Thread {
										results[it - 1] = subtask.map { entity ->
											Benchmark.benchmark(entity.phenotype)
										}
									}
									task.start()
									task
								}.forEach {
									try {
										it.join()
									}
									catch (e: InterruptedException) {
									}
								}
								results.flatMap { it }.forEachIndexed { i, b ->
									row["s$i benchmark"] = b
								}
							}
							/* Benchmark problems */
							run {
								val processors = Runtime.getRuntime().availableProcessors()
								val subtaskSize = state.problems.size / processors
								val results = Array(processors) {
									listOf<Double>()
								}
								(1..processors).map {
									val subtask = if (it == processors) {
										state.problems.subList((it - 1) * subtaskSize, state.problems.size)
									}
									else {
										state.problems.subList((it - 1) * subtaskSize, it * subtaskSize)
									}
									val task = Thread {
										results[it - 1] = subtask.map { entity ->
											Benchmark.benchmark(entity.phenotype.terrain)
										}
									}
									task.start()
									task
								}.forEach {
									try {
										it.join()
									}
									catch (e: InterruptedException) {
									}
								}
								results.flatMap { it }.forEachIndexed { i, b ->
									row["p$i benchmark"] = b
								}
							}
						}
					}

					override fun save(stats: Statistic) {
						stats.writeToFile("benchedExperiments/$filename$r.csv")
					}

				})
			}
			passedRuns++
			totalTime += time
			if (times.size > 4) times.removeAt(0)
			times.add(time)

			println("Run $r completed")
			println("Remaining time ${(((times.sum().toDouble()/times.size)*(algorithms.size*runs-passedRuns))/60000).toInt()}m")

		}

		solutionWriter.flush()
		problemWriter.flush()

	}
	println("Total time taken ${(totalTime/60000).toInt()}m")
}