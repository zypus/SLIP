package com.zypus.SLIP.views

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.SLIP.models.terrain.BlendTerrain
import com.zypus.SLIP.models.terrain.MidpointTerrain
import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.utilities.Vector2
import javafx.application.Application
import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.reactfx.EventStreams
import tornadofx.*
import java.io.File
import javax.imageio.ImageIO

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 14/04/16
 */

class MovieView() : View() {

	val canvas = Canvas(1000.0, 600.0)
	override val root = VBox()

	var play by property(false)
	fun playProperty() = getProperty(StateFragment::play)

	val setting = SimulationSetting(simulationStep = 0.2)
	val state = SimulationState(
//			slip = SLIP(
//					position = Vector2(0, 300),
//					velocity = Vector2(0, 0),
//					angle = 0.0,
//					restLength = 50.0,
//					mass = 2.0,
//					radius = 20.0,
//					springConstant = 5.0,
//										controller = SpringController(angle = { -0.02014512293491862*it.velocity.x+0.13381311880313776})
////					controller = SpringController()
//			),
			slip = SLIP(
					position = Vector2(0, 300),
					velocity = Vector2(0, 0),
					angle = 0.0,
					restLength = 49.19465,
					mass = 1.1262529,
					radius = 11.262,
					springConstant = 1.0,
					controller = SpringController(angle = { -0.015363955 * it.velocity.x + 0.03182780 }, constant = { 0.6035430 * (1-it.length/it.restLength) + 3.636863})
//					controller = SpringController()
			),
			environment = Environment(
					terrain = MidpointTerrain(0, 200.0, 1.0, 80.0, 0L)
			)
	)

	var terrain by property(state.environment.terrain)
	fun terrainProperty() = getProperty(StateFragment::terrain)

	fun Node.actionEvents() = EventStreams.eventsOf(this, ActionEvent.ACTION)

	val height = 200.0
	val displace = 80.0
	val terrains = arrayListOf(
			BlendTerrain(MidpointTerrain(0, height, 0.0, displace, 0L), MidpointTerrain(1, height, 1.0, displace, 1L)),
			BlendTerrain(MidpointTerrain(1, height, 1.0, displace, 1L), MidpointTerrain(2, height, 1.0, displace, 2L)),
			BlendTerrain(MidpointTerrain(2, height, 1.0, displace, 2L), MidpointTerrain(3, height, 1.0, displace, 3L)),
			BlendTerrain(MidpointTerrain(3, height, 1.0, displace, 3L), MidpointTerrain(4, height, 1.0, displace, 4L)),
			BlendTerrain(MidpointTerrain(4, height, 1.0, displace, 4L), MidpointTerrain(5, height, 1.0, displace, 5L)),
			BlendTerrain(MidpointTerrain(5, height, 1.0, displace, 5L), MidpointTerrain(6, height, 1.0, displace, 6L)),
			BlendTerrain(MidpointTerrain(6, height, 1.0, displace, 6L), MidpointTerrain(7, height, 1.0, displace, 7L)),
			BlendTerrain(MidpointTerrain(7, height, 1.0, displace, 7L), MidpointTerrain(8, height, 1.0, displace, 10L))
	)

	val useTerrainTimeline = true
	val useSLIPTimeline = false


	fun timelineTerrain(step: Int): Terrain {
		return when (step / 75) {
			0    -> MidpointTerrain(0, height, 1.0, displace, 0L)
			1    -> terrains[0]
			2    -> terrains[1]
			3    -> terrains[2]
			4    -> terrains[3]
			5    -> terrains[4]
			6    -> terrains[5]
			7    -> terrains[6]
			else -> terrains[6]
		}
	}

	fun timelineSLIP(step: Int, slip: SLIP): SLIP {
		return when (step / 100) {
			0    -> slip.copy(mass = 1.0, restLength = 100.0, radius = 10.0, position = Vector2(0,400))
			1    -> slip.copy(mass = 1.0, restLength = 150.0, radius = 20.0, position = Vector2(0,400))
			2    -> slip.copy(mass = 1.0, restLength = 50.0, radius = 30.0, position = Vector2(0,400))
			3    -> slip.copy(mass = 1.0, restLength = 50.0, radius = 20.0, position = Vector2(0,400))
			4    -> slip.copy(mass = 1.0, restLength = 150.0, radius = 10.0, position = Vector2(0,400))
			5    -> slip.copy(mass = 1.0, restLength = 100.0, radius = 20.0, position = Vector2(0,400))
			6    -> slip.copy(mass = 1.0, restLength = 150.0, radius = 30.0, position = Vector2(0,400))
			7    -> slip.copy(mass = 1.0, restLength = 100.0, radius = 20.0, position = Vector2(0,400))
			else -> slip.copy(mass = 1.0, restLength = 50.0, radius = 10.0, position = Vector2(0,400))
		}
	}

	val writableImage = WritableImage(1000,600)

	fun save(step: Int) {
		if (step % 74 == 0 && step < 8*75) {
			canvas.snapshot(null, writableImage)
			val file = File("Canvas${step/74}.png")
			try {ImageIO.write(SwingFXUtils.fromFXImage(writableImage,null),"png",file)} catch (e: Exception) {}
		}
	}

	init {
		val gc = canvas.graphicsContext2D
		var s = state
		var steps = 0
		gc.drawSimulationState(s, markers = false, tracking = false, linesize = 4.0)

		fun reset() {
			terrains.forEach { it.blend = 0.0 }
			play = false
			steps = 0
			s = state
			gc.drawSimulationState(s, markers = false, tracking = false, linesize = 4.0)
		}

		with(root) {
			style = "-fx-background-color: white"
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
						reset()
					}
				}
			}

			/* MARK: Canvas */
			add(canvas)

			/* MARK: Terrain */

			EventStreams.animationFrames().filter { play }
					.feedTo {
						if (s.environment.terrain is BlendTerrain) (s.environment.terrain as BlendTerrain).blend = Math.min((s.environment.terrain as BlendTerrain).blend + 0.05, 1.0)
						if (s.slip.position.y > 290) s = s.copy(slip = if (useSLIPTimeline) timelineSLIP(steps,s.slip) else s.slip,environment = s.environment.copy( terrain = if (useTerrainTimeline) timelineTerrain(steps) else s.environment.terrain))
						s = SimulationController.step(s, setting)
						gc.drawSimulationState(s, markers = false, tracking = true, linesize = 4.0)
						save(steps)
						steps++
					}
		}
	}

}

class MovieViewer : App() {
	override val primaryView = MovieView::class
}

fun main(args: Array<String>) {
	Application.launch(MovieViewer::class.java, *args)
}
