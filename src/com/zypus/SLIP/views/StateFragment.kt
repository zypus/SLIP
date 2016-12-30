package com.zypus.SLIP.views

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
import com.zypus.SLIP.models.terrain.MidpointTerrain
import com.zypus.gui.ResizableCanvas
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.util.StringConverter
import org.controlsfx.tools.Borders
import org.reactfx.EventStreams
import org.reactfx.Subscription
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

	var terrain by property(state.environment.terrain)
	fun terrainProperty() = getProperty(StateFragment::terrain)

	fun Node.actionEvents() = EventStreams.eventsOf(this, ActionEvent.ACTION)

	init {
		val gc = canvas.graphicsContext2D
		var s = state
		var steps = 0
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
						steps = 0
						s = state
						gc.drawSimulationState(s)
					}
				}
			}

			/* MARK: Canvas */
			val borderedCanvas = Borders.wrap(canvas).lineBorder().color(Color.BLACK).innerPadding(0.0).buildAll()
			EventStreams.valuesOf(borderedCanvas.boundsInLocalProperty()).feedTo {
				canvas.resize(root.width - 20, canvas.height)
				gc.drawSimulationState(s)
			}
			add(borderedCanvas)

			/* MARK: Terrain */

			EventStreams.valuesOf(terrainProperty()).feedTo {
				s = s.copy(environment = s.environment.copy(terrain = it))
				state = state.copy(environment = state.environment.copy(terrain = it))
				gc.drawSimulationState(s)
			}

			val sinus = vbox {
				spacing = 10.0
				hbox {
					alignment = Pos.CENTER_LEFT
					spacing = 10.0
					label("Height")
					val height = textfield { text = "${(terrain as? MidpointTerrain)?.height ?: 0.0}" }
					label("Roughness")
					val roughness = textfield { text = "${(terrain as? MidpointTerrain)?.roughness ?: 0.0}" }
					label("Displace")
					val displace = textfield { text = "${(terrain as? MidpointTerrain)?.displace ?: 0.0}" }
					label("Resolution")
					val resolution = textfield { text = "${(terrain as? MidpointTerrain)?.exp ?: 0.0}" }
					label("Seed")
					val seed = textfield { text = "${(terrain as? MidpointTerrain)?.seed ?: 0.0}" }

					EventStreams.merge(height.actionEvents(), roughness.actionEvents(), displace.actionEvents(), resolution.actionEvents(), seed.actionEvents()).feedTo {
						try {
							val h = height.text.toDouble()
							val r = roughness.text.toDouble()
							val d = displace.text.toDouble()
							val res = resolution.text.toDouble()
							val ss = seed.text.toDouble()
							terrain = MidpointTerrain(height = h, roughness = r, displace = d, exp = res.toInt(), seed = ss.toLong())
						}
						catch(e: NumberFormatException) {
						}
					}
				}
			}

			var subscription: Subscription? = null
			subscription = EventStreams.animationFrames().filter { play }
					.feedTo {
						if (steps < 1000) {
							if (!root.scene.window.isShowing) subscription?.unsubscribe()
							s = SimulationController.step(s, setting)
							gc.drawSimulationState(s)
							steps++
						}
					}
		}
	}

	fun changeState(s: SimulationState) {
		state = s
	}


}