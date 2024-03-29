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

fun fitnessSelector(population: List<Entity<List<Double>, *, Double, MutableList<Double>>>): (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double {
	return { it.behaviour!!.sum() }
}

fun fitnessDiversitySelector(population: List<Entity<List<Double>, *, Double, MutableList<Double>>>): (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double {
	return { e ->
		val sum = e.behaviour!!.sum()
		val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
		Math.abs(x!!.behaviour!!.sum() - sum)
	}
}

fun randomSelector(population: List<Entity<List<Double>, *, Double, MutableList<Double>>>): (Entity<List<Double>, *, Double, MutableList<Double>>) -> Double {
	return { Math.random() }
}


fun main(args: Array<String>) {
	val initial = Coevolution.initial
	val setting = Coevolution.setting
	val environment = Environment()

	val primes = arrayListOf(2L, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97)

	var totalTime = 0L
	var passedRuns = 0
	val runs = 1
	val noiseStrength = 0.0
	val cycles = 1200
	val benchmarksOn = 400
	val outputOn= 400

	val times: MutableList<Long> = arrayListOf()

	val expName = "testX"
	File("benchedExperiments/$expName").mkdir()
	File("results/$expName").mkdir()

	val fs = ::fitnessSelector
	val fds = ::fitnessDiversitySelector

	val rs = ::randomSelector

	val bfds = arrayListOf(1000,5000,10000)
	var bfdCounter = -1

//    val experimentsToRun = arrayListOf("terrain.diversity", "slip.diversity", "both.fitness", "both.fitness.adaptive", "both.diversity")
	//val experimentsToRun = arrayListOf( "both.fitness.adaptive.terrains",  "both.fitness.adaptive.terrains", "both.fitness.adaptive.terrains")
	val experimentsToRun = arrayListOf( "terrain.random")
	val experiments = experimentsToRun.flatMap {
		bfdCounter++
		primes.subList(0, runs).map { p ->
			val settings = SLIPTerrainEvolution.SLIPTerrainEvolutionSetting(noiseStrength = noiseStrength, seed = p)
			it to when (it) {
				"terrain.diversity"     -> SLIPTerrainEvolution.rule(SLIPTerrainEvolution.Selectors(fs, fds), settings)
				"slip.diversity"        -> SLIPTerrainEvolution.rule(SLIPTerrainEvolution.Selectors(fds, fs), settings)
				"both.fitness"          -> SLIPTerrainEvolution.rule(SLIPTerrainEvolution.Selectors(fs, fs), settings)
				"both.fitness.adaptive" -> SLIPTerrainEvolution.rule(SLIPTerrainEvolution.Selectors(fs, fs), settings.copy(adaptiveSolutionReproduction = true, adaptiveProblemReproduction = true, adaptiveProblemThreshold = 5000))
				"both.fitness.adaptive.terrains" -> SLIPTerrainEvolution.rule(SLIPTerrainEvolution.Selectors(fs, fs), settings.copy(adaptiveProblemReproduction = true, adaptiveProblemThreshold = 5000))
				"both.diversity"        -> SLIPTerrainEvolution.rule(SLIPTerrainEvolution.Selectors(fds, fds), settings)
				"terrain.random"        -> SLIPTerrainEvolution.rule(SLIPTerrainEvolution.Selectors(fs, rs), settings)
				else                    -> throw IllegalAccessError()
			}
		}
	}

	var r = 0

	for ((filename, rule) in experiments) {
		val solutionWriter = File("results/$expName/$filename.solution.$r.txt").printWriter()
		val problemWriter = File("results/$expName/$filename.problems.$r.txt").printWriter()
		val evolution = GenericSpringEvolution(initial, environment, setting, rule, { if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum() }) {
			if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum()
		}
		EventStreams.valuesOf(evolution.progressProperty()).feedTo {
			if (it % 0.1 == 0.0) println("%03.1f%%".format(it * 100))
			if (it == 1.0) {
				print("Writing results ...")
				evolution.solutionsProperty().get().forEach { ControllerSerializer.serialize(solutionWriter, it.genotype as List<Double>) }
				evolution.problemsProperty().get().forEach { TerrainSerializer.serialize(problemWriter, (it.phenotype as Environment).terrain) }
				println(" Done")
			}
		}

		r = 1 + (r % runs)

		println("Beginning run $r")

		val time = measureTimeMillis {
			evolution.evolve(50, 50, cycles, object : StatisticDelegate<List<Double>, SLIP, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>> {

				lateinit var solutionStatistic: Statistic
				lateinit var problemStatistic: Statistic

				val solutionBenchmarkCache: MutableMap<Int, Triple<Double, Double, Long>> = hashMapOf()
				val problemBenchmarkCache: MutableMap<Int, Triple<Double, Double, Long>> = hashMapOf()

				override fun initialize(solutionCount: Int, problemCount: Int): Statistic {
					val columns: MutableList<String> = arrayListOf("cycle", "id")
					columns.add("fitness")
					columns.add("a")
					columns.add("b")
					columns.add("c")
					columns.add("d")
					columns.add("l")
					columns.add("m")
					columns.add("stability benchmark")
					columns.add("distance benchmark")
					columns.add("benchmark mask")
					solutionStatistic = Statistic(*columns.toTypedArray())

					columns.clear()
					columns.add("cycle")
					columns.add("id")
					columns.add("fitness")
					columns.add("height")
					columns.add("power")
					columns.add("roughness")
					columns.add("displace")
					columns.add("spikiness")
					columns.add("ascension")
					columns.add("stability benchmark")
					columns.add("distance benchmark")
					columns.add("difficulty")
					columns.add("benchmark mask")
					problemStatistic = Statistic(*columns.toTypedArray())

					return solutionStatistic
				}

				override fun update(stats: Statistic, generation: Int, state: EvolutionState<List<Double>, SLIP, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>>) {

					if (generation % outputOn == 0) {
						val solutionBenchmark = if (generation % benchmarksOn == 0) {
							print("Benchmarking generation $generation ...")
							val removed = solutionBenchmarkCache.keys.toSet() - state.solutions.map { it.id }.toSet()
							removed.forEach { solutionBenchmarkCache.remove(it) }
							/* Benchmark solutions */
							run {
								val processors = Runtime.getRuntime().availableProcessors()
								val subtaskSize = state.solutions.size / processors
								val results = Array(processors) {
									listOf<Triple<Double, Double, Long>>()
								}
								(1..processors).map {
									val subtask = if (it == processors) {
										state.solutions.subList((it - 1) * subtaskSize, state.solutions.size)
									} else {
										state.solutions.subList((it - 1) * subtaskSize, it * subtaskSize)
									}
									val task = Thread {
										results[it - 1] = subtask.map { entity ->
											solutionBenchmarkCache.getOrPut(entity.id) {
												Benchmark.evaluate(entity.phenotype, Benchmark.terrainBase, Triple(0.0, 0.0, 0L), average = {
													value, i ->
													Triple(value.first / i, value.second / i, if (value.third > 0) 1L else 0L)
//												value / i
												}, sum = {
													f, s ->
													Triple(f.first + s.first, f.second + s.second, f.third.shl(1) + if (s.third > 0) 1 else 0)
//												f + s
												}, eval = {
													state, jumps, off ->
													Triple(jumps.toDouble() / 50, state.slip.position.x - off, if (state.slip.crashed) 0L else 1L)
//												Vector2(jumps.toDouble()/50, state.slip.position.x - off)
												})
											}
										}
									}
									task.start()
									task
								}.forEach {
									try {
										it.join()
									} catch (e: InterruptedException) {
									}
								}
								results.flatMap { it }
							}
						} else {
							null
						}
						/* Benchmark problems */
						val problemBenchmark = if (generation % benchmarksOn == 0) {
							val removed = problemBenchmarkCache.keys.toSet() - state.problems.map { it.id }.toSet()
							removed.forEach { problemBenchmarkCache.remove(it) }
							val result = run {
								val processors = Runtime.getRuntime().availableProcessors()
								val subtaskSize = state.problems.size / processors
								val results = Array(processors) {
									listOf<Triple<Double, Double, Long>>()
								}
								(1..processors).map {
									val subtask = if (it == processors) {
										state.problems.subList((it - 1) * subtaskSize, state.problems.size)
									} else {
										state.problems.subList((it - 1) * subtaskSize, it * subtaskSize)
									}
									val task = Thread {
										results[it - 1] = subtask.map { entity ->
											problemBenchmarkCache.getOrPut(entity.id) {
												Benchmark.evaluate(entity.phenotype.terrain, Benchmark.controllerBase, Triple(0.0, 0.0, 0L), average = {
													value, i ->
													Triple(value.first / i, value.second / i, if (value.third > 0) 1L else 0L)
//												value / i
												}, sum = {
													f, s ->
													Triple(f.first + s.first, f.second + s.second, f.third.shl(1) + if (s.third > 0) 1 else 0)
//												f + s
												}, eval = {
													state, jumps, off ->
													Triple(jumps.toDouble() / 50, state.slip.position.x - off, if (state.slip.crashed) 0L else 1L)
//												Vector2(jumps.toDouble()/50, state.slip.position.x - off)
												})
											}
										}
									}
									task.start()
									task
								}.forEach {
									try {
										it.join()
									} catch (e: InterruptedException) {
									}
								}
								results.flatMap { it }
							}
							println(" Done")
							result
						} else {
							null
						}

						state.solutions.forEachIndexed { i, entity ->
							val row = solutionStatistic.newRow()
							row["cycle"] = generation
							row["id"] = entity.id
							row["fitness"] = entity.behaviour!!.sum()
							row["a"] = entity.genotype[0]
							row["b"] = entity.genotype[1]
							row["c"] = entity.genotype[2]
							row["d"] = entity.genotype[3]
							row["l"] = entity.genotype[4]
							row["m"] = entity.genotype[5]
							solutionBenchmark?.let {
								row["stability benchmark"] = it[i].first
								row["distance benchmark"] = it[i].second
								row["benchmark mask"] = it[i].third
							}
						}
						state.problems.forEachIndexed { i, entity ->
							val row = problemStatistic.newRow()
							row["cycle"] = generation
							row["id"] = entity.id
							row["fitness"] = entity.behaviour!!.sum()
							val terrain = entity.phenotype.terrain as MidpointTerrain
							row["height"] = terrain.height
							row["power"] = terrain.power
							row["roughness"] = terrain.roughness
							row["displace"] = terrain.displace
							row["difficulty"] = terrain.displace * terrain.roughness * terrain.exp
							row["spikiness"] = TerrainDifficulty.spikiness(entity.phenotype.terrain)
							row["ascension"] = TerrainDifficulty.ascension(entity.phenotype.terrain)
							problemBenchmark?.let {
								row["stability benchmark"] = it[i].first
								row["distance benchmark"] = it[i].second
								row["benchmark mask"] = it[i].third
							}
						}
					}
				}

				override fun save(stats: Statistic) {
					solutionStatistic.writeToFile("benchedExperiments/$expName/$filename${r}_solutions.csv")
					problemStatistic.writeToFile("benchedExperiments/$expName/$filename${r}_problems.csv")
				}

			})
		}
		passedRuns++
		totalTime += time
		if (times.size > 4) times.removeAt(0)
		times.add(time)

		println("Run $r completed")
		println("Remaining time ${(((times.sum().toDouble() / times.size) * (experiments.size - passedRuns)) / 60000).toInt()}m")


		solutionWriter.flush()
		problemWriter.flush()
		solutionWriter.close()
		problemWriter.close()
	}
	println("Total time taken ${(totalTime / 60000).toInt()}m")

}
