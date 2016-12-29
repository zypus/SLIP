package com.zypus.SLIP.verification.benchmark

import com.zypus.SLIP.algorithms.SLIPTerrainEvolution
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.MidpointTerrain
import com.zypus.SLIP.models.terrain.Terrain
import mikera.vectorz.Vector2
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
		return evaluate(springController, terrainBase, 0.0, average =  {
			value, i -> value/i
		}, sum = {
			f,s -> f+s
		}, eval = {
			state, off ->
			if (state.slip.crashed) 0.0 else 1.0
		})
	}

	fun benchmark(slip: SLIP): Double {
		return evaluate(slip, terrainBase, 0.0, average =  {
			value, i -> value/i
		}, sum = {
			f,s -> f+s
		}, eval = {
			state, off ->
			if (state.slip.crashed) 0.0 else 1.0
		})
	}

	fun benchmark(terrain: Terrain): Double {
		return evaluate(terrain, controllerBase, 0.0, average =  {
			value, i -> value/i
		}, sum = {
			f,s -> f+s
		}, eval = {
			state, off ->
			if (state.slip.crashed) 0.0 else 1.0
		})
	}

	fun <T> evaluate(springController: SpringController, terrains: List<Terrain>, initial: T, average: (T,Int) -> T, sum: (T,T)-> T, eval: (SimulationState,Double)->T): T {
		return terrains.fold(initial) {
			value, terrain ->
			val environment = Environment(terrain = terrain)
			val next = average((-10..10 step 10).fold(initial) {
				s, offset ->
				val start = Initial(Vector2(offset.toDouble(), 200.0))
				val slip = SLIP(start).copy(controller = springController)
				var state = SimulationState(slip, environment)
//				for (i in 1..1000) {
//					state = SimulationController.step(state, setting)
//					if (state.slip.crashed) break
//				}
				var jumps = 0
				while (jumps < 50) {
					val before = state.slip.grounded
					state = SimulationController.step(state, SLIPTerrainEvolution.setting)
					if (before == true && state.slip.grounded == false) jumps++
					if (state.slip.crashed) break
				}
				sum (eval(state,offset.toDouble()), s)
			}, 3)
			sum(value,next)
		}
	}

	fun <T> evaluate(slip: SLIP, terrains: List<Terrain>, initial: T, average: (T,Int) -> T, sum: (T,T)-> T, eval: (SimulationState,Double)->T): T  {
		return terrains.fold(initial) {
			value, terrain ->
			val environment = Environment(terrain = terrain)
			val next = average((-10..10 step 10).fold(initial) {
				s, offset ->
				val start = Initial(Vector2(offset.toDouble(), 200.0))
				val sl = slip.copy(position = start.position, velocity = start.velocity)
				var state = SimulationState(sl, environment)
//				for (i in 1..1000) {
//					state = SimulationController.step(state, setting)
//					if (state.slip.crashed) break
//				}
				var jumps = 0
				while (jumps < 50) {
					val before = state.slip.grounded
					state = SimulationController.step(state, SLIPTerrainEvolution.setting)
					if (before == true && state.slip.grounded == false) jumps++
					if (state.slip.crashed) break
				}
				sum (eval(state,offset.toDouble()), s)
			}, 3)
			sum(value,next)
		}
	}

	fun <T> evaluate(terrain: Terrain, controllers: List<SpringController>, initial: T, average: (T,Int) -> T, sum: (T,T)-> T, eval: (SimulationState,Double)->T): T {
		val environment = Environment(terrain = terrain)
		return controllers.fold(initial) {
			value, controller ->
			val next = average((-10..10 step 10).fold(initial) {
				s, offset ->
				val start = Initial(Vector2(offset.toDouble(), 200.0))
				val slip = SLIP(start).copy(controller = controller)
				var state = SimulationState(slip, environment)
//				for (i in 1..1000) {
//					state = SimulationController.step(state, setting)
//					if (state.slip.crashed) break
//				}
				var jumps = 0
				while (jumps < 50) {
					val before = state.slip.grounded
					state = SimulationController.step(state, SLIPTerrainEvolution.setting)
					if (before == true && state.slip.grounded == false) jumps++
					if (state.slip.crashed) break
				}
				sum (eval(state,offset.toDouble()), s)
			}, 3)
			sum(value,next)
		}
	}

}