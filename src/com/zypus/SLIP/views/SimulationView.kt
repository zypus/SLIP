package com.zypus.SLIP.views

import com.zypus.math.Vector2
import com.zypus.math.percent
import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import javafx.application.Platform
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import org.reactfx.EventStreams
import tornadofx.View
import java.lang.Math.cos
import java.lang.Math.sin

/**
 * View to render all simulation state relevant information.
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 24/02/16
 */

class SimulationView : View() {

	val canvas = Canvas(500.0, 500.0)
	override val root = VBox(canvas)

	init {

		Platform.runLater {
			// Setup simulation state.
			val initial = Initial(position = Vector2(0, 210))
			//		val slip = SLIP(initial).copy(controller = SpringController { 0.021 * -it.velocity.x + 0.01 })
					val slip = SLIP(initial).copy(controller = SpringController { -0.02014512293491862 * it.velocity.x + 0.13381311880313776 })
//			val slip = SLIP(initial).copy(controller = SpringEvolution().evolve())
			//		val environment = Environment(terrain = { 40.0+20*sin(0.1*it) })
			val environment = Environment(terrain = { 30.0 })
			var state = SimulationState(slip, environment)
			val setting = SimulationSetting()

			val gc = canvas.graphicsContext2D

			EventStreams.animationFrames()
					.feedTo {
						state = SimulationController.step(state, setting)
						gc.drawSimulationState(state)
					}
		}

	}

	/**
	 * Draws the state (spring and terrain). Also renders a distance representation [drawMarkers]
	 *
	 * @param state The current simulation state.
	 */
	fun GraphicsContext.drawSimulationState(state: SimulationState) {
		// Clear the context.
		clearRect(0.0, 0.0, canvas.width, canvas.height)

		// Extract the state information.
		val (position, velocity, angle, restLength, length, springConstant, mass, radius, standPosition, flightVelocity, headPosition) = state.slip
		val (x, y) = position
		val (gravity, terrain) = state.environment

		// Shift the viewport to be centered on the slip.
		save()
		scale(1.0, -1.0)
		translate(canvas.width / 2 - x, -canvas.height)

		// Set line attributes.
		lineWidth = 1.0
		stroke = Color.BLACK

		// Draw the terrain.
		val start = x - canvas.width / 2
		val end = x + canvas.width / 2
		drawTerrain(start, end, 500, terrain)

		// Draw slip.
		// Draw the mass.
		strokeOval(x - radius, y - radius, 2 * radius, 2 * radius)

		// Draw spring.
		drawSpring(x - sin(angle) * radius, y - cos(angle) * radius, angle, length, restLength, 6, 10.percent)

		// Restore the original state.
		restore()

		// Draw markers.
		drawMarkers(start, end, canvas.width / 2 - x, 50, 10, 6.0, 3.0)
	}

	/**
	 * Uses the provided spring information and render hints to render a semi realistic 2d spring.
	 *
	 * @param xs Starting x coordinate of the spring.
	 * @param ys Starting y coordinate of the spring.
	 * @param angle Angle of the spring.
	 * @param ls Current length of the spring.
	 * @param Ls Length of the spring in rest position.
	 *
	 * @param segs Number of spring segments to be rendered.
	 * @param fixedPercentage Percentage of the spring at start and end of the spring which will be stiff.
	 */
	fun GraphicsContext.drawSpring(xs: Double, ys: Double, angle: Double, ls: Double, Ls: Double, segs: Int, fixedPercentage: Double) {
		var l = ls
		// Cap spring length.
		if (ls > Ls) {
			l = Ls
		}
		beginPath()
		// Draw the anchor.
		moveTo(xs, ys)
		lineTo(xs - sin(angle) * Ls * fixedPercentage, ys - cos(angle) * Ls * fixedPercentage)
		// Spring range.
		val Rs = Ls * (1 - 2 * fixedPercentage)
		val rs = l - Ls * 2 * fixedPercentage
		// Compute compression.
		val comp = rs / Rs
		// Compute the delta.
		val dSeg = rs / segs
		// Draw the spring segements.
		for (i in 0..segs - 1) {
			val dir = if (i % 2 == 0) 1 else -1
			val dy = dir * 0.5 * (dSeg / comp)
			//val dy = dir * 0.5 * sqrt(pow(Rs/segs,2)+pow(0.5*rs/segs,2));
			val nx = xs - sin(angle) * (Ls * fixedPercentage + (0.5 + i) * dSeg) + cos(angle) * dy
			val ny = ys - cos(angle) * (Ls * fixedPercentage + (0.5 + i) * dSeg) - sin(angle) * dy
			lineTo(nx, ny);
		}
		lineTo(xs - sin(angle) * (Ls * fixedPercentage + rs), ys - cos(angle) * (Ls * fixedPercentage + rs))
		// Draw the tip.
		lineTo(xs - sin(angle) * l, ys - cos(angle) * l)
		// Stroke path.
		stroke()
	}

	/**
	 * Uses the terrain x -> y mapping and renders a sequence of lines.
	 *
	 * @param start Left most visible x coordinate.
	 * @param end Right most visible y coordinate.
	 * @param steps Resolution of the terrain.
	 * @param terrain Function which maps x to y coordinates, which are interpreted as terrain.
	 */
	fun GraphicsContext.drawTerrain(start: Double, end: Double, steps: Int, terrain: (Double) -> Double) {
		// Compute the size of each step.
		val stepSize = (end - start) / steps
		beginPath()
		moveTo(start, terrain(start))
		// Fill in all line segments.
		for (i in 0..steps) {
			val x = start + stepSize * i
			lineTo(x, terrain(x))
		}
		// Draw the terrain.
		stroke()
	}

	/**
	 * Draws a vertical bar, with major and minor ticks, where the major ticks are labeled to represent the distance from the origin.
	 * Function must called in untransformed state, because inversion of the y axis makes it kinda difficult to draw text *not* upside down.
	 *
	 * @param start Left most visible x coordinate.
	 * @param end Right most visible y coordinate.
	 * @param offset X offset to position markers in view.
	 * @param major Distance between major ticks.
	 * @param minor Distance between minor ticks.
	 * @param majorHeight Height of the major ticks.
	 * @param minorHeight Height of the minor ticks.
	 */
	fun GraphicsContext.drawMarkers(start: Double, end: Double, offset: Double, major: Int, minor: Int, majorHeight: Double = 6.0, minorHeight: Double = 3.0) {
		var majors = start - start % major - major
		var minors = start - start % minor

		// Draw minors
		while (minors < end) {
			strokeLine(minors + offset, canvas.height - font.size, minors + offset, canvas.height - minorHeight - font.size)
			minors += minor
		}

		// Draw majors.
		while (majors < end + major) {
			strokeLine(majors + offset, canvas.height - font.size, majors + offset, canvas.height - majorHeight - font.size)
			val s = "${majors.toInt()}"
			// Using the text element to get the actual string length once rendered
			val textWidth = Text(s).layoutBounds.width
			fillText(s, majors + offset - textWidth / 2, canvas.height - 2.0)
			majors += major
		}
	}

}
