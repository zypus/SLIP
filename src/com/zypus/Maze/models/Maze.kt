package com.zypus.Maze.models

import com.zypus.utilities.LineSegment
import mikera.vectorz.Vector2

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
data class Maze(val walls: List<LineSegment>, val start: Vector2, val goal: Vector2, val orientation: Double = 0.0) {
}
