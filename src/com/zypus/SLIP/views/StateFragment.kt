package com.zypus.SLIP.views

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
import com.zypus.SLIP.models.terrain.CompositeTerrain
import com.zypus.SLIP.models.terrain.FlatTerrain
import com.zypus.SLIP.models.terrain.SinusTerrain
import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.gui.ResizableCanvas
import javafx.collections.ObservableList
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
		var subscription: Subscription? = null
		subscription = EventStreams.animationFrames().filter { play }
				.feedTo {
					if (!root.scene.window.isShowing) subscription?.unsubscribe()
					s = SimulationController.step(s, setting)
					gc.drawSimulationState(s)
				}
	}

	fun changeState(s: SimulationState) {
		state = s
	}



}