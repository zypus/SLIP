package com.zypus.Maze.simulation

import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.Robot
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.utilities.*
import mikera.vectorz.Vector2

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
object MazeNavigation {

	fun step(state: MazeNavigationState, settings: SimulationSetting): MazeNavigationState {

		val (delta, epsilon) = settings
		val (robot, maze, control) = state

		val (leftRight, upDown) = control.control(robot, maze)

		val dir = Vector2(0.0, 1.0)
		dir.rotate(robot.rot)

		val (x,y) = robot.pos
		// linear
		robot.pos.addMultiple(dir, upDown.limit(-8.0, 8.0) * delta)

		if (robot.pos.x.isInfinite()) {
			println("Ups")
		}

		//angular
		val rot = robot.rot + Angle(leftRight.limit(-0.5,0.5) * delta)

//		val moveDir = pos - robot.pos

		val circle = Circle(robot.pos, robot.radius)

		maze.walls.forEach {
			wall ->
			if (circle intersect wall) {
				robot.pos.x = x
				robot.pos.y = y
			}
		}

		val robotStar = Robot(robot.pos, rot, robot.radius)

		return MazeNavigationState(robotStar, maze, control)

	}

	fun Double.limit(min: Double, max: Double) = Math.max(min, Math.min(this, max))

}