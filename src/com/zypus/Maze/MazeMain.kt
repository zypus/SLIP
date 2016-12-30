package com.zypus.Maze

import com.zypus.Maze.view.TestMazeView
import javafx.application.Application
import tornadofx.App

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/10/2016
 */

class MazeMain : App() {
	override val primaryView = TestMazeView::class
}

fun main(args: Array<String>) {
	Application.launch(MazeMain::class.java, *args)
}