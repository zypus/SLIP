package com.zypus.SLIP.algorithms.genetic

import com.zypus.Maze.algorithms.RnnMazeEvolution
import com.zypus.Maze.controller.ARobotController
import com.zypus.Maze.models.Maze
import com.zypus.utilities.squared
import org.junit.Assert
import org.junit.Test
import org.knowm.xchart.Histogram
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 29/10/2016
 */
class UnitTest {

	@Test
	fun testSelection() {
		val random = Random(1L)

		val population = Array(10) {
			Entity<Double, Double, Double, Double>(it.toDouble(), it.toDouble().squared, "test") {
				-it
			}
		}

		val difs = population.map {
			e ->
			val sum = e.behaviour!!
			val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!! - sum) }
			Math.abs(x!!.behaviour!! - sum)
		}


		Assert.assertArrayEquals(arrayOf(1.0, 1.0, 3.0, 5.0, 7.0, 9.0, 11.0, 13.0, 15.0, 17.0), difs.toTypedArray())

		Assert.assertArrayEquals(arrayOf(17.0, 15.0, 13.0, 11.0, 9.0, 7.0, 5.0, 3.0, 1.0, 1.0), difs.sortedDescending().toTypedArray())

		val rankedPopulation = population.sortedByDescending { e ->
			val sum = e.behaviour!!
			val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!! - sum) }
			Math.abs(x!!.behaviour!! - sum)
		}
		val selections = (1..100000).map {
			Selection(1, arrayListOf(rankedPopulation.linearSelection(1.5, random) to rankedPopulation.linearSelection(1.5, random)))
		}

		val dist = selections.foldRight((1..10).map { 0 } as MutableList) {
			s, f ->
			f[s.parents[0].first.genotype.toInt()]++
			f[s.parents[0].second.genotype.toInt()]++
			f
		}

//		selections.forEach{
//			val parent = it.parents[0]
//			println("${parent.first.genotype} -> ${parent.second.genotype}")
//		}

		println(dist)

	}

	@Test
	fun testLinearSelection() {
		val populationSize = 100
		val bias = 1.999
		val selects = (1..1000).map {
			val random = it / 1000.0
			(populationSize * (bias - Math.sqrt(bias * bias - 4.0 * (bias - 1) * random)) / 2.0 / (bias - 1)).toInt()
		}

		val hist = Histogram(selects, 100)

		print(hist.getyAxisData())
	}

	@Test
	fun testReproduction() {

		fun solutionSort(population: List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>>): List<Entity<List<Double>, ARobotController, Double, MutableList<Double>>> {
			return population.sortedByDescending {
				e ->
				val sum = e.behaviour!!.sum()
				val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
				Math.abs(x!!.behaviour!!.sum() - sum)
			}
		}

		fun problemSort(population: List<Entity<List<Double>, Maze, Double, MutableList<Double>>>): List<Entity<List<Double>, Maze, Double, MutableList<Double>>> {
			return population.sortedByDescending {
				e ->
				val sum = e.behaviour!!.sum()
				val x = population.filter { it != e }.minBy { Math.abs(it.behaviour!!.sum() - sum) }
				Math.abs(x!!.behaviour!!.sum() - sum)
			}
		}

		val rule = RnnMazeEvolution.rule(RnnMazeEvolution.Selectors(::solutionSort,::problemSort))
		val state = rule.initialize(10, 1)
		Assert.assertEquals(10, state.solutions.size)
		Assert.assertEquals(1, state.problems.size)

		val hashs = state.solutions.map { it.hashCode() }

		state.solutions.forEach {
			Assert.assertEquals(0, it.behaviour!!.size)
		}

		rule.matchAndEvaluate(state)

		state.solutions.forEach {
			Assert.assertEquals(20, it.behaviour!!.size)
		}


		val afterEval = state.solutions.count {
			it.hashCode() in hashs
		}

		Assert.assertEquals(10, afterEval)

		val nextState = rule.selectAndReproduce(state)

		Assert.assertEquals(11, nextState.solutions.size)

		rule.matchAndEvaluate(nextState)

		nextState.solutions.forEach {
			Assert.assertEquals(20, it.behaviour!!.size)
		}

		state.solutions.sortedBy { it.id }.zip(nextState.solutions.sortedBy { it.id }).forEach {
			val (e1, e2) = it
			val b1 = e1.behaviour!!.sum()
			val b2 = e2.behaviour!!.sum()
			Assert.assertEquals(b1, b2,0.0)
		}

		val nextNextState = rule.selectAndReproduce(nextState)

		Assert.assertEquals(11, nextNextState.solutions.size)

		val afterEval2 = nextNextState.solutions.count {
			it.hashCode() in hashs
		}

		Assert.assertTrue(afterEval2 == 10 || afterEval2 == 9)

	}

}
