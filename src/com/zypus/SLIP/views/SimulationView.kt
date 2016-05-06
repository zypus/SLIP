package com.zypus.SLIP.views

import com.zypus.SLIP.algorithms.Coevolution
import com.zypus.SLIP.algorithms.GenericSpringEvolution
import com.zypus.SLIP.algorithms.SpringEvolution2
import com.zypus.SLIP.algorithms.SpringEvolution3
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.SinusTerrain
import com.zypus.gui.ChartFragment
import com.zypus.gui.EvolutionFragment
import com.zypus.gui.FitnessChart
import com.zypus.utilities.Vector2
import impl.org.controlsfx.skin.DecorationPane
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.chart.Chart
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.TabPane
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Modality
import org.controlsfx.tools.ValueExtractor
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationSupport
import org.reactfx.EventStreams
import tornadofx.*
import java.util.*

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
		val setting = SimulationSetting(simulationStep = 0.1)

		val initial = Initial(position = Vector2(0, 210), velocity = Vector2(0, 0))
		val environment = Environment(terrain = SinusTerrain(frequency = 0.1, shift = 0.0, amplitude = 10.0, height = 30.0))


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
			ValueExtractor.addObservableValueExtractor({ c -> c is Label }, { c -> (c as Label).textProperty() });

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
								EvolutionFragment(springEvolution2, { (this.behaviour as HashMap<*, *>).values.sumByDouble { it as Double } }).openModal()
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
								val slip = SLIP(initial).copy(controller = SpringController { SpringControl(a * it.velocity.x + b, it.springConstant) })
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
							alignment = Pos.CENTER_LEFT
							var thread: Thread? = null
							var solutionChart: Chart? = null
							val evolve = button("Evolve") {
								setOnAction {
									if (text == "Evolve") {
										text = "Stop"
										val springEvolution3 = SpringEvolution3(initial, environment, setting)
										val progress = (parent.lookup("#progress") as ProgressBar)
										progress.progressProperty().bind(springEvolution3.progressProperty())
										val generation = EventStreams.valuesOf(springEvolution3.generationProperty())
										solutionChart = FitnessChart(generation, springEvolution3.solutionsProperty()) {
											(this.behaviour as HashMap<*, *>).values.sumByDouble { it as Double }
										}
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
									}
									else {
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
									if (solutionChart != null) ChartFragment(solutionChart!!).openModal()
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
								val text3 = (parent.lookup("#c") as Label).text
								val text4 = (parent.lookup("#d") as Label).text
								val a = text1.toDouble()
								val b = text2.toDouble()
								val c = text3.toDouble()
								val d = text4.toDouble()
								// Build the state.
								val slip = SLIP(initial).copy(controller = SpringController { SpringControl(a * it.velocity.x + b, c * (1.0 - it.length / it.restLength) + d) })
								val s = SimulationState(slip, environment)

								// Set the controller for the viewer.
								StateFragment(s, setting).openModal()
							}
						}
					}
				}

				/* MARK: COEVOLUTION */

				tab("Coevolution", DecorationPane()) {
					val support = ValidationSupport()
					vbox {
						spacing = 10.0
						padding = Insets(10.0)
						hbox {
							spacing = 10.0
							alignment = Pos.CENTER_LEFT
							var thread: Thread? = null
							var solutionChart: Chart? = null
							val evolve = button("Evolve") {
								setOnAction {
									if (text == "Evolve") {
										text = "Stop"
										val springEvolution = GenericSpringEvolution(Coevolution.initial, environment, Coevolution.setting, Coevolution.rule, { if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum() }) {
											if (it.isEmpty()) Double.NEGATIVE_INFINITY else it.sum()
										}
										val progress = (parent.lookup("#progress") as ProgressBar)
										progress.progressProperty().bind(springEvolution.progressProperty())
										val generation = EventStreams.valuesOf(springEvolution.generationProperty())
//										solutionChart = FitnessChart(generation, springEvolution.solutionsProperty()) {
//											synchronized(SortLock.lock) {
//												(this.behaviour as List<*>).sumByDouble { it as? Double ?: 0.0 }
//											}
//										}
										ProgressFragment(generation, springEvolution.bestSolutionProperty(), springEvolution.bestProblemProperty()).openModal(modality = Modality.NONE)
										thread = Thread {
											val evolve = springEvolution.evolve(50, 50, 1000)
											Platform.runLater {
												(parent.parent.lookup("#a") as Label).text = "${evolve.genotype[0]}"
												(parent.parent.lookup("#b") as Label).text = "${evolve.genotype[1]}"
												(parent.parent.lookup("#c") as Label).text = "${evolve.genotype[2]}"
												(parent.parent.lookup("#d") as Label).text = "${evolve.genotype[3]}"
												text = "Evolve"
//												val s = SimulationState(SLIP(initial).copy(controller = springEvolution.bestSolution.phenotype as SpringController), springEvolution.bestProblem.phenotype as Environment)
//												SimpleStateFragment(s, setting).openModal()
											}
										}
										thread?.start()
									}
									else {
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
									if (solutionChart != null) ChartFragment(solutionChart!!).openModal()
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
								val text3 = (parent.lookup("#c") as Label).text
								val text4 = (parent.lookup("#d") as Label).text
								val a = text1.toDouble()
								val b = text2.toDouble()
								val c = text3.toDouble()
								val d = text4.toDouble()
								// Build the state.
								val slip = SLIP(initial).copy(controller = SpringController { SpringControl(a * it.velocity.x + b, c * (1.0 - it.length / it.restLength) + d) })
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
