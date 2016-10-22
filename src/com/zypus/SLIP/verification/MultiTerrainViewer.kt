package com.zypus.SLIP.verification

import com.zypus.SLIP.models.terrain.MidpointTerrain
import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.SLIP.verification.benchmark.Benchmark
import com.zypus.SLIP.verification.benchmark.TerrainSerializer
import com.zypus.SLIP.views.drawMarkers
import com.zypus.SLIP.views.drawTerrain
import com.zypus.utilities.Vector2
import com.zypus.utilities.mapParallel
import javafx.application.Application
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
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

	val canvi = (1..50).map { Canvas(120.0, 120.0) }

	override val root = VBox()

	init {
		with(root) {

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
							showTerrainPage(start, terrains)
						}
					}
				}
				button("Previous") {
					setOnAction {
						start = Math.max(start - 50, 0)
						showTerrainPage(start, terrains)
					}
				}
				button("Next") {
					setOnAction {
						start = Math.min(start + 50, terrains.size - 50)
						showTerrainPage(start, terrains)
					}
				}
			}

			val nrow = 6
			gridpane {
				hgap = 10.0
				vgap = 10.0
				addRow(0, *canvi.subList(0, nrow).toTypedArray())
				addRow(1, *canvi.subList(nrow, 2 * nrow).toTypedArray())
				addRow(2, *canvi.subList(2 * nrow, 3 * nrow).toTypedArray())
				addRow(3, *canvi.subList(3 * nrow, 4 * nrow).toTypedArray())
				addRow(4, *canvi.subList(4 * nrow, 5 * nrow).toTypedArray())
				addRow(5, *canvi.subList(5 * nrow, 6 * nrow).toTypedArray())
			}
		}
	}

	private fun showTerrainPage(start: Int, terrains: ObservableList<Terrain>) {
		val difficulty = fun (terrain: MidpointTerrain):Double {
			val benchmark = Benchmark.evaluate(terrain, Benchmark.controllerBase, Vector2(0.0, 0.0), average = {
				value, i ->
				value / i
			}, sum = {
				f, s ->
				f + s
			}, eval = {
				state, off ->
				if (state.slip.crashed) Vector2(0.0, state.slip.position.x) else Vector2(1.0, state.slip.position.x)
			})
			val dif = benchmark.y
			return dif
		}
		terrains.drop(start)
				.take(50)
				.mapParallel { it to if (it is MidpointTerrain) difficulty(it) else 0.0 }
				.sortedByDescending {
					it.second
				}
				.forEachIndexed {
					i, pair ->
					val canvas = canvi[i]
					with(canvas.graphicsContext2D) {
						fill = Color.BLACK
						clearRect(0.0, 0.0, canvas.width, canvas.height)
						translate(canvas.width / 2, canvas.height / 2)
						scale(0.25, 0.25)
						lineWidth = 4.0
						drawTerrain(-2*canvas.width, 2*canvas.width, 10000, pair.first)
						scale(4.0, 4.0)
						translate(-canvas.width / 2, -canvas.height / 2)
						translate(0.0, canvas.height / 2)
						scale(0.25, 0.25)
						drawMarkers(-2*canvas.width, 2*canvas.width, 2*canvas.width, 50, 10, 6.0, 3.0)
						scale(4.0, 4.0)
						translate(0.0, -canvas.height / 2)
//						val s = "${"%.2f".format(pair.second/1000)}"
//						val textWidth = Text(s).layoutBounds.width
//						fillText(s, canvas.width - textWidth - 10, 25.0)
						if (pair.first is MidpointTerrain) {
							val mt = pair.first as MidpointTerrain
							fill = Color.GRAY
								fillText("r = %.2f".format(mt.roughness), 10.0, 10.0)
								fillText("d = %.2f".format(mt.displace), 10.0, 25.0)
								fillText("p = %d".format(mt.exp), 10.0, 40.0)
								fillText("h = %.2f".format(mt.height), 10.0, 55.0)
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

