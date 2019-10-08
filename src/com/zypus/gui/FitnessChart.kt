package com.zypus.gui

import com.zypus.SLIP.algorithms.genetic.Entity
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import org.reactfx.EventStream
import tornadofx.observable

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/04/16
 */
class FitnessChart(val generation: EventStream<Int>, val entities: ObjectProperty<List<Entity<*,*,*,*>>>, fitness: Entity<*, *, *,*>.() -> Double): LineChart<Number,Number>(NumberAxis().apply { label = "Generation"; tickUnit = 1.0 }, NumberAxis().apply { label = "Fitness" }) {

	init {
		title = "GA"

		/* Initialize the data series and include a dummy data point in order to trigger the legend symbol generation. */
		val bestFitness = XYChart.Series<Number, Number>().apply { name = "Best fitness"; data.add(XYChart.Data(0, 0)) }
		val averageFitness = XYChart.Series<Number, Number>().apply { name = "Average fitness"; data.add(XYChart.Data(0, 0)) }
		val worstFitness = XYChart.Series<Number, Number>().apply { name = "Worst fitness"; data.add(XYChart.Data(0, 0)) }

		/* Each time the generation gets updated add another entry to the series */
		generation.feedTo {

			/* Add new data point to each series.*/
			Platform.runLater {
				/* Obtain the current set of entityList. */
				val entityList = entities.get()

				if (!entityList.isEmpty()) {
					/* Compute the best, average and worst fitness. */
					val (best, sum, worst) = entityList.fold(Triple(Double.NEGATIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY)) {
						fold, entity ->
						val f = entity.fitness()
						Triple(Math.max(fold.first, f), fold.second + f, Math.min(fold.third, f))
					}

					/* Create a new data point in the corresponding series. */
					bestFitness.data.add(XYChart.Data(it, best))
					averageFitness.data.add(XYChart.Data(it, sum / entityList.size))
					worstFitness.data.add(XYChart.Data(it, worst))
				}
			}
		}

		/* Connect the series to the chart */
		this.data = arrayListOf(worstFitness, averageFitness, bestFitness).observable()

		/* Remove the dummy data point again. */
		bestFitness.data.clear()
		averageFitness.data.clear()
		worstFitness.data.clear()

	}

}
