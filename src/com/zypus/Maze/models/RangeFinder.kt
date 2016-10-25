package com.zypus.Maze.models

import com.zypus.utilities.LineSegment
import com.zypus.utilities.Vector2
import com.zypus.utilities.intersect

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
object RangeFinder {

	fun findRange(robot: Robot, maze: Maze, dir: Vector2): Double {
		val pos = robot.pos
		val walls = maze.walls
		val line = LineSegment(from = pos, to = pos+dir*10000)
		val closestWall = walls.minBy {
			wall ->
			(wall intersect line)?.distanceTo(pos) ?: Double.MAX_VALUE
		}
		return closestWall?.intersect(line)?.distanceTo(pos) ?: -1.0
	}

}