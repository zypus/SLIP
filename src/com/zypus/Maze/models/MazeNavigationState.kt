package com.zypus.Maze.models

import com.zypus.Maze.controller.ARobotController

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/10/2016
 */
data class MazeNavigationState(val robot: Robot, val maze: Maze, val controller: ARobotController) {
}