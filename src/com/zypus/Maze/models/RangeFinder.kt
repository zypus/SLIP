package com.zypus.Maze.models

import com.zypus.utilities.LineSegment
import com.zypus.utilities.distanceTo
import com.zypus.utilities.intersect
import mikera.vectorz.Vector2

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
		val target = pos.clone()
		target.addMultiple(dir, 10000.0)
		val line = LineSegment(from = pos.clone(), to = target)
		val closestWall = walls.minBy {
			wall ->
			(wall intersect line)?.distanceTo(pos) ?: Double.MAX_VALUE
		}
		return closestWall?.intersect(line)?.distanceTo(pos) ?: -1.0
	}

}