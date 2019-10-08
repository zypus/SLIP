package com.zypus.SLIP

import com.zypus.SLIP.views.SimulationView
import javafx.application.Application
import tornadofx.App

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 24/02/16
 */

class Main : App() {
	override val primaryView = SimulationView::class
}

fun main(args: Array<String>) {
	Application.launch(Main::class.java, *args)
}
