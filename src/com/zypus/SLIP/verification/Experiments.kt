package com.zypus.SLIP.verification

import com.zypus.SLIP.algorithms.Coevolution
import com.zypus.SLIP.algorithms.GenericSpringEvolution
import com.zypus.SLIP.algorithms.SLIPTerrainEvolution
import com.zypus.SLIP.algorithms.genetic.Entity
import com.zypus.SLIP.algorithms.genetic.EvolutionState
import com.zypus.SLIP.controllers.StatisticDelegate
import com.zypus.SLIP.models.Environment
import com.zypus.SLIP.models.SLIP
import com.zypus.SLIP.models.Statistic
import com.zypus.SLIP.models.terrain.MidpointTerrain
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

fun fitnessSelector(population: List<Entity<List<Double>, *, Double, MutableList<Double>>>): (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double  {
	return {it.behaviour!!.sum()}
}

fun fitnessDiversitySelector(population: List<Entity<List<Double>, *, Double, MutableList<Double>>>): (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double {
	return {e ->
		val sum = e.behaviour!!.sum()
		val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
		Math.abs(x!!.behaviour!!.sum() - sum)}
}


fun main(args: Array<String>) {
	val initial = Coevolution.initial
	val setting = Coevolution.setting
	val environment = Environment()

	var totalTime = 0L;
	var passedRuns = 0;
	val runs = 10;
	val noiseStrength = 0.0
	val cycles = 4000

	val times: MutableList<Long> = arrayListOf()

	val expName = "nonoise4000"
	File("benchedExperiments/$expName").mkdir()
	File("results/$expName").mkdir()

	val fs = ::fitnessSelector
	val fds = ::fitnessDiversitySelector

	val algorithms = mapOf("terrain.diversity" to SLIPTerrainEvolution.rule(fs,fds, noiseStrength = noiseStrength), "slip.diversity" to SLIPTerrainEvolution.rule(fds,fs, noiseStrength = noiseStrength), "both.fitness" to SLIPTerrainEvolution.rule(fs,fs, adaptiveReproduction = true, noiseStrength = noiseStrength), "both.diversity" to SLIPTerrainEvolution.rule(fds,fds, noiseStrength = noiseStrength))
	for ((filename, rule) in algorithms) {
		val solutionWriter = File("results/$expName/$filename.solution.txt").printWriter()
		val problemWriter = File("results/$expName/$filename.problems.txt").printWriter()
		val evolution = GenericSpringEvolution(initial, environment, setting, rule, { if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum() }) {
			if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum()
		}
		EventStreams.valuesOf(evolution.progressProperty()).feedTo {
			if (it % 0.1  == 0.0) println("%03.1f%%".format(it * 100))
			if (it == 1.0) {
				evolution.solutionsProperty().get().forEach { ControllerSerializer.serialize(solutionWriter, it.genotype as List<Double>) }
				evolution.problemsProperty().get().forEach { TerrainSerializer.serialize(problemWriter, (it.phenotype as Environment).terrain) }
			}
		}

		for (r in 1..runs) {

			println("Beginning run $r")

			val time = measureTimeMillis {
				evolution.evolve(50, 50, cycles, object : StatisticDelegate<List<Double>, SLIP, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>> {
					override fun initialize(solutionCount: Int, problemCount: Int): Statistic {
						val columns: MutableList<String> = arrayListOf("cycle")
						repeat(solutionCount + 1) {
							columns.add("s$it fitness")
							columns.add("s$it a")
							columns.add("s$it b")
							columns.add("s$it c")
							columns.add("s$it d")
							columns.add("s$it l")
							columns.add("s$it m")
							columns.add("s$it benchmark")
						}
						repeat(problemCount + 1) {
							columns.add("p$it fitness")
							columns.add("p$it height")
							columns.add("p$it power")
							columns.add("p$it roughness")
							columns.add("p$it displace")
							columns.add("p$it spikiness")
							columns.add("p$it ascension")
							columns.add("p$it benchmark")
							columns.add("p$it difficulty")
						}
						return Statistic(*columns.toTypedArray())
					}

					override fun update(row: Statistic.Row, generation: Int, state: EvolutionState<List<Double>, SLIP, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>>) {
						row["cycle"] = generation
						state.solutions.forEachIndexed { i, entity ->
							row["s$i fitness"] = entity.behaviour!!.sum()
							row["s$i a"] = entity.genotype[0]
							row["s$i b"] = entity.genotype[1]
							row["s$i c"] = entity.genotype[2]
							row["s$i d"] = entity.genotype[3]
							row["s$i l"] = entity.genotype[4]
							row["s$i m"] = entity.genotype[5]
						}
						state.problems.forEachIndexed { i, entity ->
							row["p$i fitness"] = entity.behaviour!!.sum()
							val terrain = entity.phenotype.terrain as MidpointTerrain
							row["p$i height"] = terrain.height
							row["p$i power"] = terrain.power
							row["p$i roughness"] = terrain.roughness
							row["p$i displace"] = terrain.displace
							row["p$i difficulty"] = terrain.displace*terrain.roughness*terrain.exp+terrain.height
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
						stats.writeToFile("benchedExperiments/$expName/$filename$r.csv")
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