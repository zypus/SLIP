package com.zypus.SLIP.views

import com.zypus.SLIP.algorithms.SpringEvolution2
import com.zypus.SLIP.algorithms.SpringEvolution3
import com.zypus.SLIP.models.*
import com.zypus.gui.EvolutionFragment
import com.zypus.utilities.Vector2
import impl.org.controlsfx.skin.DecorationPane
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.TabPane
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import org.controlsfx.tools.ValueExtractor
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationSupport
import tornadofx.*

/**
 * View to render all simulation state relevant information.
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 24/02/16
 */

class SimulationView : View() {

	override val root = VBox()

	init {

		//		var state: SimulationState? = null
		val setting = SimulationSetting()

		val initial = Initial(position = Vector2(0, 210), velocity = Vector2(0, 0))
		val environment = Environment(terrain = { 30 + 10 * Math.sin(0.1 * it) })


		// Lengthy computation
		// Setup simulation state.
		//		Thread {
		//
		//			//		val slip = SLIP(initial).copy(controller = SpringController { 0.021 * -it.velocity.x + 0.01 })
		//								val slip = SLIP(initial).copy(controller = SpringController { -0.02014512293491862 * it.velocity.x + 0.13381311880313776 })
		////						val slip = SLIP(initial)
		//
		////			val slip = SLIP(initial).copy(controller = springEvolution2.evolve())
		//			//					val environment = Environment(terrain = { 40.0+10*sin(0.1*it) })
		//			state = SimulationState(slip, environment)
		//
		//			Platform.runLater { StateFragment(state!!, setting).openModal() }
		//		}.apply { name = "evolution" }.start()


		with(root) {

			val numberValidator = org.controlsfx.validation.Validator.createRegexValidator("Please enter a valid number!", "^-?\\d+(,\\d+)*(\\.\\d+(e\\d+)?)?$", Severity.ERROR)
			ValueExtractor.addObservableValueExtractor({c -> c is Label}, {c -> (c as Label).textProperty()});

			tabpane {
				tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

				/* MARK: UNCONTROLLED */

				tab("Uncontrolled", DecorationPane()) {
					val support = ValidationSupport()
					vbox {
						spacing = 10.0
						padding = Insets(10.0)

						hbox {
							alignment = Pos.CENTER_LEFT
							label("Angle")
							textfield("0") {
								id = "angle"
								support.registerValidator(this, numberValidator)
							}
						}
						button {
							disableProperty().bind(support.invalidProperty())
							text = "Deploy"
							setOnAction {
								val text = (parent.lookup("#angle") as TextField).text
								val a = text.toDouble()
								// Build the state.
								val slip = SLIP(initial).copy(controller = SpringController { SpringControl(a, it.springConstant) })
								val s = SimulationState(slip, environment)

								// Set the controller for the viewer.
								StateFragment(s, setting).openModal()
							}
						}
					}
				}

				/* MARK: MINIMAL CONTROL */

				tab("Minimal control", DecorationPane()) {
					val support = ValidationSupport()
					vbox {
						spacing = 10.0
						padding = Insets(10.0)
						hbox {
							alignment = Pos.CENTER_LEFT
							textfield("-0.02014512293491862") {
								id = "a"
								support.registerValidator(this, numberValidator)
							}
							label("velocity.x + ")
							textfield("0.13381311880313776") {
								id = "b"
								support.registerValidator(this, numberValidator)
							}
						}
						button {
							disableProperty().bind(support.invalidProperty())
							text = "Deploy"
							setOnAction {
								val text1 = (parent.lookup("#a") as TextField).text
								val text2 = (parent.lookup("#b") as TextField).text
								val a = text1.toDouble()
								val b = text2.toDouble()
								// Build the state.
								val slip = SLIP(initial).copy(controller = SpringController { SpringControl(a * it.velocity.x + b, it.springConstant) })
								val s = SimulationState(slip, environment)

								// Set the controller for the viewer.
								StateFragment(s, setting).openModal()
							}
						}
					}
				}

				/* MARK: EVOLUTION (a*vx+b, k)*/

				tab("Minimal evolution", DecorationPane()) {
					val support = ValidationSupport()
					vbox {
						spacing = 10.0
						padding = Insets(10.0)
						button("Evolve") {
							setOnAction {
								val springEvolution2 = SpringEvolution2(initial, environment, setting)
								EvolutionFragment(springEvolution2, { this.behavior.values.sumByDouble { it as Double } }).openModal()
								Thread {
									val evolve = springEvolution2.evolve()
									Platform.runLater {
										(parent.lookup("#a") as Label).text = "${evolve.genotype[0]}"
										(parent.lookup("#b") as Label).text = "${evolve.genotype[1]}"
									}
								}.start()
							}
						}
						hbox {
							alignment = Pos.CENTER_LEFT
							label("a") {
								style = "-fx-font-weight: bold;"
								id = "a"
								support.registerValidator(this, numberValidator)
							}
							label(" velocity.x + ")
							label("b") {
								style = "-fx-font-weight: bold;"
								id = "b"
								support.registerValidator(this, numberValidator)
							}
						}
						button {
							disableProperty().bind(support.invalidProperty())
							text = "Deploy"
							setOnAction {
								val text1 = (parent.lookup("#a") as Label).text
								val text2 = (parent.lookup("#b") as Label).text
								val a = text1.toDouble()
								val b = text2.toDouble()
								// Build the state.
								val slip = SLIP(initial).copy(controller = SpringController { SpringControl(a * it.velocity.x + b,it.springConstant) })
								val s = SimulationState(slip, environment)

								// Set the controller for the viewer.
								StateFragment(s, setting).openModal()
							}
						}
					}
				}

				/* MARK: EVOLUTION (a*vx+b, c*k+d)*/

				tab("Basic evolution", DecorationPane()) {
					val support = ValidationSupport()
					vbox {
						spacing = 10.0
						padding = Insets(10.0)
						hbox {
							spacing = 10.0
							alignment = Pos.CENTER
							var evolutionFragment: EvolutionFragment? = null
							var thread: Thread? = null
							val evolve = button("Evolve") {
								setOnAction {
									if (text == "Evolve") {
										text = "Stop"
										val springEvolution3 = SpringEvolution3(initial, environment, setting)
										val progress = (parent.lookup("#progress") as ProgressBar)
										progress.progressProperty().bind(springEvolution3.progressProperty())
										evolutionFragment = EvolutionFragment(springEvolution3, { this.behavior.values.sumByDouble { it as Double } })
										thread = Thread {
											val evolve = springEvolution3.evolve()
											Platform.runLater {
												(parent.parent.lookup("#a") as Label).text = "${evolve.genotype[0]}"
												(parent.parent.lookup("#b") as Label).text = "${evolve.genotype[1]}"
												(parent.parent.lookup("#c") as Label).text = "${evolve.genotype[2]}"
												(parent.parent.lookup("#d") as Label).text = "${evolve.genotype[3]}"
												text = "Evolve"
											}
										}
										thread?.start()
									} else {
										thread?.interrupt()
									}
								}
							}
							progressBar {
								id = "progress"
								visibleProperty().bind(evolve.textProperty().isEqualTo("Stop"))
							}
							button("Show") {
								visibleProperty().bind(evolve.textProperty().isEqualTo("Stop"))
								setOnAction {
									evolutionFragment?.openModal()
								}
							}
						}

						hbox {
							alignment = Pos.CENTER_LEFT
							label("a") {
								style = "-fx-font-weight: bold;"
								id = "a"
								support.registerValidator(this, numberValidator)
							}
							label(" velocity.x + ")
							label("b") {
								style = "-fx-font-weight: bold;"
								id = "b"
								support.registerValidator(this, numberValidator)
							}

							label(" , ")
							label("c") {
								style = "-fx-font-weight: bold;"
								id = "c"
								support.registerValidator(this, numberValidator)
							}
							label(" compression + ")
							label("d") {
								style = "-fx-font-weight: bold;"
								id = "d"
								support.registerValidator(this, numberValidator)
							}
						}
						button {
							disableProperty().bind(support.invalidProperty())
							text = "Deploy"
							setOnAction {
								val text1 = (parent.lookup("#a") as Label).text
								val text2 = (parent.lookup("#b") as Label).text
								val a = text1.toDouble()
								val b = text2.toDouble()
								// Build the state.
								val slip = SLIP(initial).copy(controller = SpringController { SpringControl(a * it.velocity.x + b, it.springConstant) })
								val s = SimulationState(slip, environment)

								// Set the controller for the viewer.
								StateFragment(s, setting).openModal()
							}
						}
					}
				}

			}

		}

	}


}
