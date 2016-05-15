package com.zypus.SLIP.verification

import com.zypus.SLIP.algorithms.Coevolution
import com.zypus.SLIP.algorithms.GenericSpringEvolution
import com.zypus.SLIP.algorithms.SLIPNoveltyCoevolution
import com.zypus.SLIP.algorithms.TerrainNoveltyCoevolution
import com.zypus.SLIP.algorithms.genetic.EvolutionState
import com.zypus.SLIP.controllers.StatisticDelegate
import com.zypus.SLIP.models.Environment
import com.zypus.SLIP.models.SpringController
import com.zypus.SLIP.models.Statistic
import org.reactfx.EventStreams
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
	val runs = 100;

	for ((filename, rule) in mapOf("tnc" to TerrainNoveltyCoevolution.rule, "snc" to SLIPNoveltyCoevolution.rule, "c" to Coevolution.rule )) {
		val evolution = GenericSpringEvolution(initial, environment, setting, rule, { if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum() }) {
			if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum()
		}
		EventStreams.valuesOf(evolution.progressProperty()).feedTo {
			println("%03.1f".format(it * 100))
		}

		for (r in 1..runs) {

			println("Beginning run $r")

			totalTime += measureTimeMillis {
				evolution.evolve(50, 50, 1000, object : StatisticDelegate<List<Double>, SpringController, Double, MutableList<Double>, List<Double>, Environment, Double, MutableList<Double>> {
					override fun initialize(solutionCount: Int, problemCount: Int): Statistic {
						var columns: MutableList<String> = arrayListOf("generation")
						repeat(solutionCount + 1) {
							columns.add("s$it fitness")
							columns.add("s$it a")
							columns.add("s$it b")
							columns.add("s$it c")
							columns.add("s$it d")
						}
						repeat(problemCount + 1) {
							columns.add("p$it fitness")
							columns.add("p$it height")
							columns.add("p$it spikiness")
							columns.add("p$it ascension")
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
					}

					override fun save(stats: Statistic) {
						stats.writeToFile("$filename$r.csv")
					}

				})
			}
			passedRuns++

			println("Run $r completed")
			println("Remaining time ${((totalTime/passedRuns*(3*runs-passedRuns))/60000).toInt()}m")

		}

	}
//
//	TestTerrains.terrains.forEach {
//		// Build the state.
//		val slip = SLIP(initial).copy(controller = entity.phenotype)
//		var s = SimulationState(slip, environment.copy(terrain = it))
//		for (i in 1..2000) {
//			s = SimulationController.step(s, setting)
//			if (s.slip.crashed) break
//		}
//		println("${if (s.slip.crashed) "X" else " "} ${s.slip.position.x} <- $it")
//	}
}