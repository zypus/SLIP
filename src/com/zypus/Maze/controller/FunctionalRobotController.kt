package com.zypus.Maze.controller

import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.Robot

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
class FunctionalRobotController(val leftFunctions: List<(Double)->Double>, val rightFunctions: List<(Double) -> Double>) : ARobotController() {

	override fun control(robot: Robot, maze: Maze): ARobotController.Steering {

		val inputs = inputs(robot, maze)
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

	override fun copy(): ARobotController {
		return FunctionalRobotController(leftFunctions, rightFunctions)
	}
}
