package com.zypus.Maze.controller

import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.Robot
import com.zypus.utilities.Vector2
import com.zypus.utilities.deg
import com.zypus.utilities.rangeTo

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
abstract class ARobotController {

	val rangeFinders = arrayListOf(
			Vector2(-1, 0), // left                  \  |  /
			Vector2(-1, 1).unit(), // left up         \ | /
			Vector2(0, 1), // up                   ----   ----
			Vector2(1, 1).unit(), // right up           |
			Vector2(1, 0), // right                     |
			Vector2(0, -1)  // down
	)

	val PI = Math.PI

	val pieGoalFinder = arrayListOf(
			-45.deg..45.deg,
			45.deg..135.deg,
			135.deg..-135.deg,
			-135.deg..-45.deg
	)


	data class Steering(val leftRight: Double, val upDown: Double)

	abstract fun control(robot: Robot, maze: Maze): Steering

}