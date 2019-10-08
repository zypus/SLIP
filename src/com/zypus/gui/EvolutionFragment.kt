package com.zypus.gui

import com.zypus.SLIP.algorithms.Evolution
import com.zypus.SLIP.algorithms.genetic.Entity
import javafx.application.Platform
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.layout.VBox
import org.controlsfx.control.NotificationPane
import org.reactfx.EventStreams
import tornadofx.Fragment
import tornadofx.observable

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 14/04/16
 */

class EvolutionFragment(evolution: Evolution, fitness: Entity<*, *, *,*>.() -> Double) : Fragment() {

	val chart = LineChart<Number, Number>(NumberAxis().apply { label = "Generation"; tickUnit = 1.0 }, NumberAxis().apply { label = "Fitness" }).apply { title = "GA" }
	val notificationPane = NotificationPane(chart)
	override val root = VBox(notificationPane)

	init {
		/* Initialize the data series and include a dummy data point in order to trigger the legend symbol generation. */
		val bestFitness = XYChart.Series<Number, Number>().apply { name = "Best fitness"; data.add(XYChart.Data(0, 0)) }
		val averageFitness = XYChart.Series<Number, Number>().apply { name = "Average fitness"; data.add(XYChart.Data(0, 0)) }
		val worstFitness = XYChart.Series<Number, Number>().apply { name = "Worst fitness"; data.add(XYChart.Data(0,0)) }

		/* Each time the generation gets updated add another entry to the series */
		EventStreams.valuesOf(evolution.generationProperty()).feedTo {

			/* Add new data point to each series.*/
			Platform.runLater {
				/* Obtain the current set of solutions. */
				val solutions = evolution.solutionsProperty().get()

				if (!solutions.isEmpty()) {
					/* Compute the best, average and worst fitness. */
					val (best, sum, worst) = solutions.fold(Triple(Double.NEGATIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY)) {
						fold, entity ->
						val f = entity.fitness()
						Triple(Math.max(fold.first, f), fold.second + f, Math.min(fold.third, f))
					}

					/* Create a new data point in the corresponding series. */
					bestFitness.data.add(XYChart.Data(it, best))
					averageFitness.data.add(XYChart.Data(it, sum / solutions.size))
					worstFitness.data.add(XYChart.Data(it, worst))
				}
			}
		}

		/* Connect the series to the chart */
		chart.data = arrayListOf(worstFitness, averageFitness, bestFitness).observable()

		/* Remove the dummy data point again. */
		bestFitness.data.clear()
		averageFitness.data.clear()
		worstFitness.data.clear()

		/* Inform about completed evolution. */
		EventStreams.valuesOf(evolution.finishedProperty()).filter { it }.feedTo {
			Platform.runLater {
				notificationPane.show("Evolution completed!")
			}
		}
	}

}
