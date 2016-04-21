package com.zypus.SLIP.views

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
import com.zypus.SLIP.models.terrain.CompositeTerrain
import com.zypus.SLIP.models.terrain.FlatTerrain
import com.zypus.SLIP.models.terrain.SinusTerrain
import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.gui.ResizableCanvas
import com.zypus.utilities.percent
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.util.StringConverter
import org.controlsfx.tools.Borders
import org.reactfx.EventStreams
import tornadofx.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 14/04/16
 */

class StateFragment(var state: SimulationState, setting: SimulationSetting) : Fragment() {

	val canvas = ResizableCanvas(500.0, 400.0)
	override val root = VBox()

	var play by property(false)
	fun playProperty() = getProperty(StateFragment::play)

	fun Node.actionEvents() = EventStreams.eventsOf(this, ActionEvent.ACTION)

	init {
		val gc = canvas.graphicsContext2D
		var s = state
		gc.drawSimulationState(s)
		with(root) {
			alignment = Pos.CENTER
			padding = Insets(10.0)
			spacing = 10.0
			hbox {
				alignment = Pos.CENTER
				spacing = 10.0
				button {
					textProperty().bindBidirectional(playProperty(), object : StringConverter<Boolean>() {
						override fun toString(b: Boolean): String = if (b) "Pause" else "Play"

						override fun fromString(string: String): Boolean = string == "Pause"
					})
					setOnAction { play = !play }
				}
				button("Reset") {
					setOnAction {
						play = false
						s = state
						gc.drawSimulationState(s)
					}
				}
			}

			/* MARK: Canvas */
			val borderedCanvas = Borders.wrap(canvas).lineBorder().color(Color.BLACK).innerPadding(0.0).buildAll()
			EventStreams.valuesOf(borderedCanvas.boundsInLocalProperty()).feedTo {
				canvas.resize(root.width-20, canvas.height)
				gc.drawSimulationState(s)
			}
			add(borderedCanvas)

			/* MARK: Terrain */

			val terrain: ObservableList<Terrain> = arrayListOf<Terrain>(FlatTerrain(30.0)).observable()

			EventStreams.changesOf(terrain).feedTo {
				val compositeTerrain = CompositeTerrain(*terrain.toTypedArray())
				s = s.copy(environment = s.environment.copy(terrain = compositeTerrain))
				state = state.copy(environment = state.environment.copy(terrain = compositeTerrain))
				gc.drawSimulationState(s)
			}

			val sinus = vbox {
				spacing = 10.0
				hbox {
					alignment = Pos.CENTER_LEFT
					spacing = 10.0
					label("Height")
					val h = textfield { text = "30.0" }
					h.actionEvents().feedTo {
						val i = parent.childrenUnmodifiable.indexOf(this)
						try {
							val height = h.text.toDouble()
							terrain[i] = FlatTerrain(height)
						}
						catch(e: NumberFormatException) {
						}
					}
				}
			}
			val add = button("Add") {
				setOnAction {
					terrain.add(SinusTerrain(frequency = 0.1, shift = 0.0, amplitude = 10.0))
					sinus.apply {
						hbox {
							alignment = Pos.CENTER
							spacing = 10.0
							label("Frequency")
							val freq = textfield { text = "0.1" }
							label("Shift")
							val shift = textfield { text = "0.0" }
							label("Amplitude")
							val amp = textfield { text = "10.0" }

							button("Remove") {
								setOnAction {
									val container = parent.parent
									val i = container.childrenUnmodifiable.indexOf(parent)
									terrain.removeAt(i)
									(container as VBox).children.removeAt(i)
								}
							}

							EventStreams.merge(freq.actionEvents(), shift.actionEvents(), amp.actionEvents()).feedTo {
								val i = parent.childrenUnmodifiable.indexOf(this)
								try {
									val f = freq.text.toDouble()
									val sh = shift.text.toDouble()
									val a = amp.text.toDouble()
									terrain[i] = SinusTerrain(frequency = f, shift = sh, amplitude = a)
								}
								catch(e: NumberFormatException) {
								}
							}
						}
					}
				}
			}
			add.fire()
		}
		EventStreams.animationFrames().filter { play }
				.feedTo {
					s = SimulationController.step(s, setting)
					gc.drawSimulationState(s)
				}
	}

	fun changeState(s: SimulationState) {
		state = s
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
		val (position, velocity, angle, restLength, length, springConstant, mass, radius, standPosition) = state.slip
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
		drawSpring(x - Math.sin(angle) * radius, y - Math.cos(angle) * radius, angle, length, restLength, 6, 10.percent)

		// Restore the original state.
		restore()

		// Draw markers.
		drawMarkers(start, end, canvas.width / 2 - x, 50, 10, 6.0, 3.0)

		// Draw Info.
		drawStateInfo(state)
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
		lineTo(xs - Math.sin(angle) * Ls * fixedPercentage, ys - Math.cos(angle) * Ls * fixedPercentage)
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
			val nx = xs - Math.sin(angle) * (Ls * fixedPercentage + (0.5 + i) * dSeg) + Math.cos(angle) * dy
			val ny = ys - Math.cos(angle) * (Ls * fixedPercentage + (0.5 + i) * dSeg) - Math.sin(angle) * dy
			lineTo(nx, ny);
		}
		lineTo(xs - Math.sin(angle) * (Ls * fixedPercentage + rs), ys - Math.cos(angle) * (Ls * fixedPercentage + rs))
		// Draw the tip.
		lineTo(xs - Math.sin(angle) * l, ys - Math.cos(angle) * l)
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
	fun GraphicsContext.drawTerrain(start: Double, end: Double, steps: Int, terrain: Terrain) {
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

	fun GraphicsContext.drawStateInfo(state: SimulationState) {
		//		val slip = state.slip
		//		// E_pot = m * g * h
		//		val potentialEnergy = slip.mass * -state.environment.gravity.y * (slip.position.y - slip.radius - state.environment.terrain(slip.position.y))
		//		// E_kin = 0.5 * m * v^2
		//		val kineticEnergy = 0.5 * slip.mass * slip.velocity.norm2
		//		// E_ela = 0.5 * k * dl^2
		//		val elasticEnergy = 0.5 * slip.springConstant * Math.pow(slip.restLength - slip.length, 2.0)
		//		val totalEnergy = potentialEnergy + kineticEnergy + elasticEnergy
		//		var i = 0
		//		fillText("Potential Energy: ${(potentialEnergy * 100).toInt() / 100.0}", 10.0, 20.0 * ++i)
		//		fillText("Kinetic Energy  : ${(kineticEnergy * 100).toInt() / 100.0}", 10.0, 20.0 * ++i)
		//		fillText("Elastic Energy  : ${(elasticEnergy * 100).toInt() / 100.0}", 10.0, 20.0 * ++i)
		//		fillText("------------------------", 10.0, 20.0 * ++i)
		//		fillText("Total Energy    : ${(totalEnergy * 100).toInt() / 100.0}", 10.0, 20.0 * ++i)
	}

}