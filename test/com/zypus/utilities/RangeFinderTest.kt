package com.zypus.utilities

import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.RangeFinder
import com.zypus.Maze.models.Robot
import mikera.vectorz.Vector2
import org.junit.Test

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 12/11/2016
 */
class RangeFinderTest {

	@Test
	fun rangeFinderTest() {
		val robot = Robot(Vector2(0.0,0.0), 0.deg, 5.0)
		val maze = Maze(arrayListOf(LineSegment(Vector2(-1.0,-1.0), Vector2(1.0,-1.0))), Vector2(0.0,0.0), Vector2(0.0,0.0))
		println(RangeFinder.findRange(robot, maze, Vector2(1.0,0.0)))
		println(RangeFinder.findRange(robot, maze, Vector2(-1.0,0.0)))
		println(RangeFinder.findRange(robot, maze, Vector2(0.0,1.0)))
		println(RangeFinder.findRange(robot, maze, Vector2(0.0,-1.0)))
	}

}
