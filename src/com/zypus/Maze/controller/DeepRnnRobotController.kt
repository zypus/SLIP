package com.zypus.Maze.controller

import com.fasterxml.jackson.annotation.JsonIgnore
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.Robot
import com.zypus.rnn.DeepRnn
import com.zypus.rnn.Hidden
import com.zypus.utilities.MatrixProxy
import com.zypus.utilities.toVector

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 19/11/2016
 */
class DeepRnnRobotController(val proxies: List<MatrixProxy>): ARobotController() {

	@JsonIgnore
	var rnn: DeepRnn? = null

	override fun start() {
		val rnnCount = (proxies.size-2) / 4
		val hiddenLayers = (1..rnnCount).map {
			val current = proxies.slice(4*(it-1)..(4*it)-1)
			Hidden(current[0].toVector(), current[1].toMatrix(), current[2].toMatrix(), current[3].toVector())
		}
		rnn = DeepRnn(hiddenLayers, proxies[proxies.size-2].toMatrix(), proxies[proxies.size - 1].toVector())
	}

	override fun control(robot: Robot, maze: Maze): ARobotController.Steering {
		val inputs = inputs(robot, maze).dropLast(1)

		val output = rnn!!.step(inputs.toVector())

		latestOutput = output.toList()

		return ARobotController.Steering(output[0], output[1])
	}

	override fun copy(): ARobotController {
		return DeepRnnRobotController(proxies)
	}

}
