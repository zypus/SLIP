package com.zypus.Maze.view

import com.zypus.Maze.controller.FunctionalRobotController
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.Robot
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.utilities.LineSegment
import com.zypus.utilities.deg
import javafx.geometry.Insets
import tornadofx.View
import tornadofx.hbox
import tornadofx.plusAssign
import java.lang.Math.random

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/10/2016
 */
class TestMazeView : View() {
	override val root = hbox {

		padding = Insets(5.0)

		val walls = arrayListOf(
				LineSegment(mikera.vectorz.Vector2(0.0, 0.0), mikera.vectorz.Vector2(0.0, 500.0)),
				LineSegment(mikera.vectorz.Vector2(0.0, 500.0), mikera.vectorz.Vector2(500.0, 500.0)),
				LineSegment(mikera.vectorz.Vector2(0.0, 0.0), mikera.vectorz.Vector2(500.0, 0.0)),
				LineSegment(mikera.vectorz.Vector2(500.0, 0.0), mikera.vectorz.Vector2(500.0, 500.0)),
				LineSegment(mikera.vectorz.Vector2(100.0, 400.0), mikera.vectorz.Vector2(400.0, 100.0))
		)

		val start = mikera.vectorz.Vector2(100.0, 100.0)
		val goal = mikera.vectorz.Vector2(400.0, 400.0)

		val maze = Maze(walls, start, goal)

		val robot = Robot(start, 0.deg, 5.0)

		val control = FunctionalRobotController(
				leftFunctions = arrayListOf(
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() }
				),
				rightFunctions = arrayListOf(
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() },
						{ x -> random() }
				)
		)

		val state = MazeNavigationState(robot, maze, control)

		val setting = SimulationSetting()

		val frag = MazeFragment(this, state, setting)

		this += frag

	}
}
