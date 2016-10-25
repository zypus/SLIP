//package com.zypus.toy
//
//import com.zypus.SLIP.algorithms.SortLock
//import com.zypus.SLIP.algorithms.genetic.Selection
//import com.zypus.SLIP.algorithms.genetic.builder.evolution
//import com.zypus.SLIP.algorithms.genetic.crossover
//import com.zypus.SLIP.algorithms.genetic.linearSelection
//import com.zypus.SLIP.algorithms.genetic.mutate
//import com.zypus.SLIP.models.terrain.MidpointTerrain
//import com.zypus.SLIP.models.terrain.Terrain
//
///**
// * TODO Add description
// *
// * @author fabian <zypus@users.noreply.github.com>
// *
// * @created 24/05/16
// */
//
//fun crossOverMutation(mother: List<Double>, father: List<Double>, cRate: Double, mRate: Double, sRate: Double, change: Double, bounds: List<Pair<Double, Double>>): List<Double> {
//	val crossover = mother.crossover(father, cRate)
//	return crossover.mutate(mRate) {
//		i, e ->
//		val bound = if (bounds.size > i) {
//			bounds[i]
//		}
//		else {
//			bounds[0]
//		}
//		when (random.nextDouble()) {
//		// decrease/increase the value a bit
//			in 0.0..(1.0 - sRate) / 2           -> {
//				Math.max(bound.first, e - change)
//			}
//			in (1.0 - sRate) / 2..(1.0 - sRate) -> {
//				Math.min(e + change, bound.second)
//			}
//			else                                -> {
//				resolveBound(bound)
//			}
//		}
//	}.toList()
//}
//
//val rule = evolution<List<Double>, Double, Double, MutableList<Double>, Terrain, Terrain, Double, MutableList<Double>> {
//
//	solution = {
//
//		initialize = {
//			arrayListOf(Math.random(),Math.random())
//		}
//
//		mapping = {
//			it[0] + it[1] / 2.0
//		}
//
//		reproduce = {
//			m, f -> crossOverMutation(m,f,1.0,1.0,0.4,0.0001,arrayListOf(0.0 to 1.0))
//		}
//
//		select = { population ->
//			val rankedPopulation = population.sortedByDescending { e ->
//				val sum = e.behaviour!!.sum()
//				val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
//				Math.abs(x!!.behaviour!!.sum() - sum)
//			}
//			Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5) to rankedPopulation.linearSelection(1.5)))
//		}
//
//		refine = {
//			el, n ->
//			synchronized(SortLock.lock) {
//				el.toList().sortedByDescending {
//					it.behaviour!!.sum()
//				}.take(n)
//			}
//		}
//
//		behaviour = {
//
//			initialize = { arrayListOf<Double>() }
//
//			store = {
//				e, o, b ->
//				e.add(b)
//				e.takeLast(1) as MutableList<Double>
//			}
//
//		}
//
//	}
//
//	singularProblem = MidpointTerrain(6,30.0,0.8,30.0)
//
//	test = {
//
//
//
//	}
//
//}