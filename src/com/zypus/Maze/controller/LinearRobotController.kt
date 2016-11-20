package com.zypus.Maze.controller

import com.fasterxml.jackson.annotation.JsonIgnore
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.Robot

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 28/10/2016
 */
class LinearRobotController(val leftWeights: List<Double>, val rightWeights: List<Double>): ARobotController() {

	@JsonIgnore
	var leftFunctions: List<(Double) -> Double>? = null
	@JsonIgnore
	var rightFunctions: List<(Double)->Double>? = null

	override fun control(robot: Robot, maze: Maze): Steering {
		if (leftFunctions == null) {
			leftFunctions = leftWeights.map { w -> { x: Double -> w * x } }
			rightFunctions = rightWeights.map { w -> { x: Double -> w * x } }
		}

		val inputs = inputs(robot, maze)
		assert(leftFunctions!!.size == inputs.size)
		assert(rightFunctions!!.size == inputs.size)
		// compute the left and right steering based on the given functions
		val leftRight = inputs.zip(leftFunctions!!).fold(0.0) {
			current, next ->
			val (input, function) = next
			current + function(input)
		}
		val upDown = inputs.zip(rightFunctions!!).fold(0.0) {
			current, next ->
			val (input, function) = next
			current + function(input)
		}
		if (leftRight.isInfinite() || upDown.isInfinite()) {
			println("Ups")
		}
		return Steering(leftRight, upDown)
	}

	override fun copy(): ARobotController {
		return LinearRobotController(leftWeights, rightWeights)
	}
}