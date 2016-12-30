package com.zypus.Maze.controller

import com.fasterxml.jackson.annotation.JsonIgnore
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.Robot
import com.zypus.rnn.Rnn
import com.zypus.utilities.MatrixProxy
import com.zypus.utilities.toVector

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 29/10/2016
 */
class RnnRobotController(val ih: MatrixProxy, val wxh: MatrixProxy, val whh: MatrixProxy, val why: MatrixProxy, val bh: MatrixProxy, val `by`: MatrixProxy): ARobotController() {

	@JsonIgnore
	var rnn: Rnn? = null

	override fun start() {
		rnn = Rnn(ih.toVector(), wxh.toMatrix(), whh.toMatrix(), why.toMatrix(), bh.toVector(), `by`.toVector())
	}

	override fun control(robot: Robot, maze: Maze): Steering {
		val inputs = inputs(robot, maze)

		val output = rnn!!.step(inputs.toVector())

		return Steering(output[0], output[1])
	}

	override fun copy(): RnnRobotController {
		return RnnRobotController(ih, wxh, whh, why, bh, `by`)
	}
}