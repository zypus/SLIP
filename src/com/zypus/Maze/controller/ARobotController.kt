package com.zypus.Maze.controller

import com.fasterxml.jackson.annotation.JsonIgnore
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.RangeFinder
import com.zypus.Maze.models.Robot
import com.zypus.utilities.angleTo
import com.zypus.utilities.deg
import com.zypus.utilities.rangeTo
import com.zypus.utilities.rotate
import mikera.vectorz.Vector2

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */

abstract class ARobotController {

	@JsonIgnore
	val rangeFinders = arrayListOf(
			Vector2(-1.0, 0.0), // left                  	 \  |  /
			Vector2(-1.0, 1.0).toNormal(), // left up         \ | /
			Vector2(0.0, 1.0), // up                   	   ----   ----
			Vector2(1.0, 1.0).toNormal(), // right up           |
			Vector2(1.0, 0.0), // right                     	|
			Vector2(0.0, -1.0)  // down
	)

	@JsonIgnore
	val pieGoalFinder = arrayListOf(
			-45.deg..45.deg,
			45.deg..135.deg,
			135.deg..-135.deg,
			-135.deg..-45.deg
	)

	data class Steering(val leftRight: Double, val upDown: Double)

	var latestInputs: List<Double> = arrayListOf()
	var latestOutput: List<Double> = arrayListOf()

	fun inputs(robot: Robot, maze: Maze): List<Double> {
		val ranges = rangeFinders.map { // rotate the range finder by the rotation of the robot
			val ret = it.clone()
			ret.rotate(robot.rot)
			ret
		}.map { // compute the range to the nearest wall per range finder
			dir ->
			RangeFinder.findRange(robot, maze, dir) / 100.0
		}

		// compute the pie goal finder information
		val dirToGoal = maze.goal.clone()
		dirToGoal.sub(robot.pos)
		val orientation = Vector2(0.0,1.0)
		orientation.rotate(robot.rot)
		val angle = dirToGoal angleTo orientation
		val pies = pieGoalFinder.map {
			if (it.contains(angle)) 5.0 else 0.0
		}
		assert(pies.count { it == 1.0 } == 1)
		latestInputs = arrayListOf(*ranges.toTypedArray(), *pies.toTypedArray(), 1.0)
		return latestInputs
	}

	open fun start() {

	}

	abstract fun control(robot: Robot, maze: Maze): Steering

	abstract fun copy(): ARobotController

}
