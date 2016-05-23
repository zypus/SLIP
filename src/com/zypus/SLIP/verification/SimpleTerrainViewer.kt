package com.zypus.SLIP.verification

import com.zypus.SLIP.models.terrain.MidpointTerrain
import com.zypus.SLIP.views.drawMarkers
import com.zypus.SLIP.views.drawTerrain
import javafx.application.Application
import javafx.scene.canvas.Canvas
import javafx.scene.layout.VBox
import org.reactfx.EventStreams
import tornadofx.App
import tornadofx.View
import tornadofx.add

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 18/05/16
 */

class SimpleTerrainView : View() {

	val canvas = Canvas(500.0, 250.0)

	override val root = VBox()

	init {
		with(root) {
			add(canvas)

			val terrain = MidpointTerrain(10, 50.0, 0.9, 50.0)

			var pos = 0
			var dir = -1

			EventStreams.animationFrames().feedTo {
				pos += dir*5
				if (Math.abs(pos) > 2500) dir *= -1
				with(canvas.graphicsContext2D) {
					clearRect(0.0, 0.0, canvas.width, canvas.height)
					save()
					translate(canvas.width / 2 - pos, canvas.height / 2)
					drawTerrain(pos - canvas.width / 2, pos + canvas.width / 2, 100, terrain)
					restore()
					drawMarkers(pos- canvas.width/2, pos + canvas.width/2, canvas.width / 2 - pos, 50, 10, 6.0, 3.0)
				}
			}

		}
	}

}

class SimpleTerrainViewer : App() {
	override val primaryView = SimpleTerrainView::class
}

fun main(args: Array<String>) {
	Application.launch(SimpleTerrainViewer::class.java, *args)
}


