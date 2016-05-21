package com.zypus.SLIP.verification

import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.SLIP.verification.benchmark.TerrainSerializer
import com.zypus.SLIP.views.drawMarkers
import com.zypus.SLIP.views.drawTerrain
import javafx.application.Application
import javafx.scene.canvas.Canvas
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.reactfx.EventStreams
import tornadofx.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 18/05/16
 */

class TerrainView: View() {

	val canvas = Canvas(500.0, 250.0)

	override val root = VBox()

	init {
		with(root) {
			add(canvas)

			val terrains = arrayListOf<Terrain>().observable()

			button("Load") {
				setOnAction {
					val fileChooser = FileChooser()
					fileChooser.title = "Open terrain file"
					val file = fileChooser.showOpenDialog(primaryStage)
					if (file != null) {
						val lines = file.reader().readLines() as MutableList<String>
						terrains.clear()
						while (!lines.isEmpty()) {
							val terrain = TerrainSerializer.deserialize(lines)
							if (terrain != null) {
								terrains.add(terrain)
							}
						}
					}
				}
			}

			combobox(values = terrains) {
				EventStreams.valuesOf(selectionModel.selectedItemProperty()).feedTo {
					if (it != null) {
						with(canvas.graphicsContext2D) {
							clearRect(0.0,0.0,canvas.width,canvas.height)
							translate(canvas.width/2,canvas.height/2)
							drawTerrain(-canvas.width/2, canvas.width/2, 10000, it)
							translate(-canvas.width / 2, -canvas.height / 2)
							drawMarkers(-canvas.width / 2, canvas.width / 2, canvas.width / 2, 50, 10, 6.0, 3.0)
						}
					}
				}
			}
		}
	}

}

class TerrainViewer : App() {
	override val primaryView = TerrainView::class
}

fun main(args: Array<String>) {
	Application.launch(TerrainViewer::class.java, *args)
}

