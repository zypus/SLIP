package com.zypus.Maze

import com.zypus.Maze.view.SimpleMazeView
import javafx.application.Application
import tornadofx.App

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/10/2016
 */

class SimpleMazeMain : App() {
	override val primaryView = SimpleMazeView::class
}

fun main(args: Array<String>) {
	Application.launch(SimpleMazeMain::class.java, *args)
}