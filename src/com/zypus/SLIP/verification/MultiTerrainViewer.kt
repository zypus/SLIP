package com.zypus.SLIP.verification

import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.SLIP.verification.benchmark.TerrainSerializer
import com.zypus.SLIP.views.drawMarkers
import com.zypus.SLIP.views.drawTerrain
import javafx.application.Application
import javafx.scene.canvas.Canvas
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import tornadofx.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 18/05/16
 */

class MultiTerrainView : View() {

	val canvi = (1..50).map { Canvas(130.0, 130.0) }

	override val root = VBox()

	init {
		with(root) {

			gridpane {
				hgap = 10.0
				vgap = 10.0
				addRow(0,*canvi.subList(0,10).toTypedArray())
				addRow(1,*canvi.subList(10,20).toTypedArray())
				addRow(2,*canvi.subList(20,30).toTypedArray())
				addRow(3,*canvi.subList(30,40).toTypedArray())
				addRow(4,*canvi.subList(40,50).toTypedArray())
			}

			val terrains = arrayListOf<Terrain>().observable()

			var start = 0

			hbox {
				spacing = 10.0
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
							start = 0
							terrains.drop(start).take(50).forEachIndexed { i, terrain ->
								val canvas = canvi[i]
								with(canvas.graphicsContext2D) {
									clearRect(0.0, 0.0, canvas.width, canvas.height)
									translate(canvas.width / 2, canvas.height / 2)
									scale(0.5, 0.5)
									drawTerrain(-canvas.width, canvas.width, 10000, terrain)
									scale(2.0, 2.0)
									translate(-canvas.width / 2, -canvas.height / 2)
									translate(0.0, canvas.height / 2)
									scale(0.5, 0.5)
									drawMarkers(-canvas.width, canvas.width, canvas.width, 50, 10, 6.0, 3.0)
									scale(2.0, 2.0)
									translate(0.0, -canvas.height / 2)
								}
							}
						}
					}
				}
				button("Previous") {
					setOnAction {
						start = Math.max(start - 50, 0)
						terrains.drop(start).take(50).forEachIndexed { i, terrain ->
							val canvas = canvi[i]
							with(canvas.graphicsContext2D) {
								clearRect(0.0, 0.0, canvas.width, canvas.height)
								translate(canvas.width / 2, canvas.height / 2)
								scale(0.5, 0.5)
								drawTerrain(-canvas.width, canvas.width, 10000, terrain)
								scale(2.0, 2.0)
								translate(-canvas.width / 2, -canvas.height / 2)
								translate(0.0, canvas.height / 2)
								scale(0.5, 0.5)
								drawMarkers(-canvas.width, canvas.width, canvas.width, 50, 10, 6.0, 3.0)
								scale(2.0, 2.0)
								translate(0.0, -canvas.height / 2)
							}
						}
					}
				}
				button("Next") {
					setOnAction {
						start = Math.min(start + 50, terrains.size - 50)
						terrains.drop(start).take(50).forEachIndexed { i, terrain ->
							val canvas = canvi[i]
							with(canvas.graphicsContext2D) {
								clearRect(0.0, 0.0, canvas.width, canvas.height)
								translate(canvas.width / 2, canvas.height / 2)
								scale(0.5, 0.5)
								drawTerrain(-canvas.width, canvas.width, 10000, terrain)
								scale(2.0, 2.0)
								translate(-canvas.width / 2, -canvas.height / 2)
								translate(0.0, canvas.height / 2)
								scale(0.5, 0.5)
								drawMarkers(-canvas.width, canvas.width, canvas.width, 50, 10, 6.0, 3.0)
								scale(2.0, 2.0)
								translate(0.0, -canvas.height / 2)
							}
						}
					}
				}
			}
		}
	}

}

class MultiTerrainViewer : App() {
	override val primaryView = MultiTerrainView::class
}

fun main(args: Array<String>) {
	Application.launch(MultiTerrainViewer::class.java, *args)
}

