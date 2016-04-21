package com.zypus.gui

import javafx.scene.canvas.Canvas

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 21/04/16
 */
class ResizableCanvas(iw: Double, ih: Double): Canvas(iw, ih) {

	override fun isResizable() = true

	override fun resize(w: Double, h: Double) {
		width = w
		height = h
	}

	override fun prefWidth(h: Double) = width
	override fun prefHeight(w: Double) = height

}