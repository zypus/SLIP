package com.zypus.SLIP.verification.benchmark

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.MidpointTerrain
import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.utilities.Vector2
import java.io.File

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 15/05/16
 */
object Benchmark {

	val setting = SimulationSetting()

	val terrainBase by lazy {
//		val file = File("ShortTerrainBenchmark.txt")
//		val terrains: MutableList<Terrain> = arrayListOf()
//		val lines = file.reader().readLines() as MutableList<String>
//		while (!lines.isEmpty()) {
//			val terrain = TerrainSerializer.deserialize(lines)
//			if (terrain != null) {
//				terrains.add(terrain)
//			}
//		}
//		terrains
		val terrains: MutableList<Terrain> = arrayListOf()
		var s = 0L
		for (r in arrayOf(0.7,0.8,1.0)) {
			for (d in arrayOf(40.0,60.0,90.0)) {
				for(e in arrayOf(4,6,8,10)) {
					terrains += MidpointTerrain(e, d*r+2*e, r, d, s)
					s++
				}
			}
		}
		terrains
	}

	val controllerBase by lazy {
		val file = File("ShortControllerBenchmark.txt")
		val controllers: MutableList<SpringController> = arrayListOf()
		val lines = file.reader().readLines() as MutableList<String>
		while (!lines.isEmpty()) {
			val controller = ControllerSerializer.deserialize(lines)
			if (controller != null) {
				controllers.add(controller)
			}
		}
		controllers
	}

	fun benchmark(springController: SpringController): Double {
		return evaluate(springController, terrainBase) {
			state, off -> if (state.slip.crashed) 0.0 else 1.0
		}
	}

	fun benchmark(slip: SLIP): Double {
		return evaluate(slip, terrainBase) {
			state, off ->
			if (state.slip.crashed) 0.0 else 1.0
		}
	}

	fun benchmark(terrain: Terrain): Double {
		return evaluate(terrain, controllerBase) {
			state, off ->
			if (state.slip.crashed) 0.0 else 1.0
		}
	}

	fun evaluate(springController: SpringController, terrains: List<Terrain>, eval: (SimulationState,Double)->Double): Double {
		return terrains.sumByDouble {
			val environment = Environment(terrain = it)
			(-10..10 step 10).sumByDouble {
				val initial = Initial(Vector2(it, 200))
				val slip = SLIP(initial).copy(controller = springController)
				var state = SimulationState(slip, environment)
				for (i in 1..1000) {
					state = SimulationController.step(state, setting)
					if (state.slip.crashed) break
				}
				eval(state,it.toDouble())
			} / 3.0
		}
	}

	fun evaluate(slip: SLIP, terrains: List<Terrain>, eval: (SimulationState, Double) -> Double): Double {
		return terrains.sumByDouble {
			val environment = Environment(terrain = it)
			(-10..10 step 10).sumByDouble {
				val initial = Initial(Vector2(it, 200))
				val s = slip.copy(position = initial.position, velocity = initial.velocity)
				var state = SimulationState(s, environment)
				for (i in 1..1000) {
					state = SimulationController.step(state, setting)
					if (state.slip.crashed) break
				}
				eval(state, it.toDouble())
			} / 3.0
		}
	}

	fun evaluate(terrain: Terrain, controllers: List<SpringController>, eval: (SimulationState, Double) -> Double): Double {
		val environment = Environment(terrain = terrain)
		return controllers.sumByDouble {
			c ->
			(-10..10 step 10).sumByDouble {
				val initial = Initial(Vector2(it, 200))
				val slip = SLIP(initial).copy(controller = c)
				var state = SimulationState(slip, environment)
				for (i in 1..1000) {
					state = SimulationController.step(state, setting)
					if (state.slip.crashed) break
				}
				eval(state, it.toDouble())
			} / 3.0
		}
	}

}