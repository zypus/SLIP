package com.zypus.Maze.view

import com.zypus.rnn.DeepRnn
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import tornadofx.Fragment
import tornadofx.plusAssign
import tornadofx.vbox

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 19/11/2016
 */
class LiveRnnFragment : Fragment() {
	val canvas = Canvas(700.0, 300.0)
	override val root = vbox {
		this += canvas
	}

	fun update(rnn: DeepRnn, input: List<Double>, output: List<Double>) {
		rnn.render(canvas.graphicsContext2D, input, output)
	}

}

fun DeepRnn.render(gc: GraphicsContext, input: List<Double>, output: List<Double>) {
	gc.apply {
		fill = Color.WHITE
		gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
		val xOffset = 30.0
		val yOffset = 60.0
		val y = hiddenLayers.size + 2

		hiddenLayers.forEachIndexed { l, hidden ->
			hidden.bh.forEachIndexed { j, d ->
				if (d != 0.0) {
					stroke = if (d < 0.0) Color.RED else Color.GREEN
					beginPath()
					moveTo(xOffset, (l + 1) * yOffset)
					lineTo((j + 2) * xOffset, (l + 2) * yOffset)
					stroke()
				}
			}
			// Bias
			fill = Color.DARKGREEN
			fillCircle(xOffset, (l + 1) * yOffset, 10.0)

			hidden.Wxh.forEachIndexed { i, aVector ->
				aVector.forEachIndexed { j, d ->
					if (d != 0.0) {
						stroke = if (d < 0.0) Color.RED else Color.GREEN
						beginPath()
						moveTo((j + 2) * xOffset, (l + 1) * yOffset)
						lineTo((i + 2) * xOffset, (l + 2) * yOffset)
						stroke()
					}
				}
			}
			hidden.Whh.forEachIndexed { i, aVector ->
				aVector.forEachIndexed { j, d ->
					if (d != 0.0) {
						stroke = if (d < 0.0) Color.RED else Color.GREEN
						beginPath()
						moveTo((j + 2) * xOffset, (l + 2) * yOffset)
						val upOrDown = if (j < i) 1.5 else 2.5
						val cx1 = if (j == i) (j + 1) * xOffset else (j + 2) * xOffset
						val cx2 = if (j == i) (i + 3) * xOffset else (i + 2) * xOffset
						bezierCurveTo(
								cx1, // cx1
								(l + upOrDown) * yOffset, // cy1
								cx2, // cx2
								(l + upOrDown) * yOffset, // cy2
								(i + 2) * xOffset, // x
								(l + 2) * yOffset // y
						)
						stroke()
					}
				}
			}
		}
		by.forEachIndexed { j, d ->
			if (d != 0.0) {
				stroke = if (d < 0.0) Color.RED else Color.GREEN
				beginPath()
				moveTo(xOffset, (y - 1) * yOffset)
				lineTo((j + 2) * xOffset, (y) * yOffset)
				stroke()
			}
		}
		// y Bias
		fill = Color.DARKGREEN
		fillCircle(xOffset, (y - 1) * yOffset, 10.0)

		Why.forEachIndexed { i, aVector ->
			aVector.forEachIndexed { j, d ->
				if (d != 0.0) {
					stroke = if (d < 0.0) Color.RED else Color.GREEN
					beginPath()
					moveTo((j + 2) * xOffset, (y - 1) * yOffset)
					lineTo((i + 2) * xOffset, y * yOffset)
					stroke()
				}
			}
		}

		// Nodes:

		input.forEachIndexed { i, d ->
			fill = if (d == 0.0) Color.GRAY else if (d == 5.0) Color.GREEN else Color.RED.interpolate(Color.GREEN, d / 5.0)
			fillCircle((i + 2) * xOffset, yOffset, 10.0)
		}

		hiddenLayers.forEachIndexed { l, hidden ->
			hidden.h.forEachIndexed { i, d ->
				fill = if (d < 0) Color.RED.interpolate(Color.GRAY, 1.0 + d) else if (d > 0) Color.GRAY.interpolate(Color.GREEN, d) else Color.GRAY
				fillCircle((i + 2) * xOffset, (l + 2) * yOffset, 10.0)
			}
		}

		output.forEachIndexed { i, d ->
			fill = if (d < 0) Color.RED.interpolate(Color.GRAY, (5.0 + d) / 5) else if (d > 0) Color.GRAY.interpolate(Color.GREEN, d / 5) else Color.GRAY
			fillCircle((i + 2) * xOffset, y * yOffset, 10.0)
		}

	}.toString()
}

fun GraphicsContext.fillCircle(cx: Double, cy: Double, r: Double) {
	fillOval(cx - r, cy - r, 2 * r, 2 * r)
}
