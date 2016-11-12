package com.zypus.Maze.models

import com.zypus.utilities.Angle
import mikera.vectorz.Vector2

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
data class Robot(val pos: Vector2, val rot: Angle, val radius: Double)
