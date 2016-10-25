package com.zypus.Maze.controller

import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.RangeFinder
import com.zypus.Maze.models.Robot
import com.zypus.utilities.Vector2

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
class FunctionalRobotController(val leftFunctions: List<(Double)->Double>, val rightFunctions: List<(Double) -> Double>) : ARobotController() {

	override fun control(robot: Robot, maze: Maze): ARobotController.Steering {

		// compute the ranges of all range finders
		val ranges = rangeFinders.map { // rotate the range finder by the rotation of the robot
			it.rotate(robot.rot)
		}.map { // compute the range to the nearest wall per range finder
			dir ->
			RangeFinder.findRange(robot, maze, dir)
		}

		// compute the pie goal finder information
		val dirToGoal = maze.goal - robot.pos
		val angle = dirToGoal angleTo (Vector2(0,1).rotate(robot.rot))
		val pies = pieGoalFinder.map {
			if (angle in it) 1.0 else 0.0
		}
		assert(pies.count { it==1.0 } == 1)
		val inputs = arrayListOf(*ranges.toTypedArray(),*pies.toTypedArray())
		assert(leftFunctions.size == inputs.size)
		assert(rightFunctions.size == inputs.size)
		// compute the left and right steering based on the given functions
		val leftRight = inputs.zip(leftFunctions).fold(0.0) {
			current, next ->
			val (input, function) = next
			current + function(input)
		}
		val upDown = inputs.zip(rightFunctions).fold(0.0) {
			current, next ->
			val (input, function) = next
			current + function(input)
		}
		if (leftRight.isInfinite() || upDown.isInfinite()) {
			println("Ups")
		}
		return Steering(leftRight,upDown)
	}
}