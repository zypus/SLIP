package com.zypus.Maze.view

import com.zypus.Maze.algorithms.RnnMazeEvolution
import com.zypus.Maze.controller.ARobotController
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.Robot
import com.zypus.Maze.simulation.MazeNavigation
import com.zypus.gui.ResizableCanvas
import com.zypus.utilities.component1
import com.zypus.utilities.component2
import com.zypus.utilities.deg
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.Fragment

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 06/11/2016
 */
class MatchupFragment : Fragment() {

	val canvas = ResizableCanvas(500.0, 500.0)
	override val root = VBox(canvas)

	val markerRadius = 2.5
	val debug = true

	fun drawMatchup(gc: GraphicsContext, maze: Maze, controller: ARobotController) {

		gc.clearRect(0.0, 0.0, canvas.width, canvas.height)

//		gc.fill = Color(1.0,1.0,1.0,0.05)  // transparent white

//		gc.fillRect(0.0,0.0, canvas.width, canvas.height)

		gc.stroke = Color.BLACK
		gc.fill = Color.BLACK

		val scale = 10.0

		val (walls, start, goal) = maze

		gc.beginPath()
		walls.forEach {
			wall ->
			val (from, to) = wall
			gc.moveTo(scale*from.x, scale *from.y)
			gc.lineTo(scale *to.x, scale *to.y)
		}
		gc.stroke()

		gc.fillOval(scale *start.x - markerRadius, scale *start.y - markerRadius, 2 * markerRadius, 2 * markerRadius)
		gc.fillRect(scale *goal.x - markerRadius, scale *goal.y - markerRadius, 2 * markerRadius, 2 * markerRadius)

		gc.beginPath()
		gc.moveTo(scale *start.x, scale *start.y)

		val robot = Robot(maze.start.clone(), maze.orientation.deg, 0.5)

		gc.stroke = Color.GREEN

		controller.start()
		var state = MazeNavigationState(robot = robot, maze = maze, controller = controller)
		for (i in 1..500) {
			state = MazeNavigation.step(state, RnnMazeEvolution.setting)
			if (i % 25 == 0) {
				val (x, y) = state.robot.pos
				gc.lineTo(scale *x, scale *y)
			}
		}
		gc.stroke()
	}
}