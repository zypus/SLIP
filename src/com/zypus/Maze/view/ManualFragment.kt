package com.zypus.Maze.view

import com.zypus.Maze.controller.ARobotController
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.RangeFinder
import com.zypus.Maze.models.Robot
import com.zypus.Maze.simulation.MazeNavigation
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.gui.ResizableCanvas
import com.zypus.utilities.Angle
import com.zypus.utilities.angleTo
import com.zypus.utilities.deg
import com.zypus.utilities.rotate
import javafx.scene.Parent
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import mikera.vectorz.Vector2
import org.reactfx.EventStreams
import org.reactfx.Subscription
import tornadofx.Fragment

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/10/2016
 */
class ManualFragment(val parent: Parent, var maze: Maze, setting: SimulationSetting, width: Double = 500.0, height: Double = 500.0) : Fragment() {

	val canvas = ResizableCanvas(width, height)
	override val root = VBox(canvas)

	init {

		val gc = canvas.graphicsContext2D

		var upDown = 0.0
		var leftRight = 0.0

		val manualController = object : ARobotController() {
			override fun control(robot: Robot, maze: Maze): Steering {
				return Steering(leftRight, upDown)
			}

			override fun copy(): ARobotController {
				return this
			}
		}

		val robot = Robot(maze.start, 0.deg, 5.0)
		manualController.start()
		var s = MazeNavigationState(robot, maze, manualController)
		drawState(gc, s)

		with(canvas) {
			setOnKeyPressed {
				when (it.text.first()) {
					'w' -> upDown = 10.0
					's' -> upDown = -10.0
					'a' -> leftRight = -5.0
					'd' -> leftRight = 5.0
					'r' -> s = MazeNavigationState(robot, maze, manualController)
				}
			}
			setOnKeyReleased {
				when (it.text.first()) {
					'w' -> upDown = 0.0
					's' -> upDown = 0.0
					'a' -> leftRight = 0.0
					'd' -> leftRight = 0.0
				}
			}
		}

		var subscription: Subscription? = null
		subscription = EventStreams.animationFrames()
				.feedTo {
					canvas.requestFocus()
					if (!root.parent.scene.window.isShowing) {
						subscription!!.unsubscribe()
					}
					else {
						s = MazeNavigation.step(s, setting)
						drawState(gc, s)
					}
				}
	}

	val markerRadius = 2.5
	val debug = true

	fun drawState(gc: GraphicsContext, state: MazeNavigationState) {

		gc.clearRect(0.0, 0.0, canvas.width, canvas.height)

		gc.stroke = Color.BLACK
		gc.fill = Color.BLACK

		val (robot, maze, control) = state

		val (walls, start, goal) = maze

		gc.beginPath()
		walls.forEach {
			wall ->
			val (from, to) = wall
			gc.moveTo(from.x, from.y)
			gc.lineTo(to.x, to.y)
		}
		gc.stroke()

		gc.fillOval(start.x - markerRadius, start.y - markerRadius, 2 * markerRadius, 2 * markerRadius)
		gc.fillRect(goal.x - markerRadius, goal.y - markerRadius, 2 * markerRadius, 2 * markerRadius)

		// draw robot
		gc.strokeOval(
				robot.pos.x - robot.radius,
				robot.pos.y - robot.radius,
				2 * robot.radius,
				2 * robot.radius
		)

		gc.beginPath()
		val dir = robot.pos.clone()
		val orientation = Vector2(0.0, 1.0)
		orientation.rotate(robot.rot)
		dir.addMultiple(orientation, robot.radius)
		gc.moveTo(robot.pos.x, robot.pos.y)
		gc.lineTo(dir.x, dir.y)
		gc.stroke()

		if (debug) {
			gc.stroke = Color.GREEN
			gc.fill = Color.GREEN
			control.rangeFinders.map { // rotate the range finder by the rotation of the robot
				val ret = it.clone()
				ret.rotate(robot.rot)
				ret
			}.forEach { // compute the range to the nearest wall per range finder
				dir ->
				val range = RangeFinder.findRange(robot, maze, dir)
				val from = robot.pos.clone()
				from.addMultiple(dir, robot.radius)
				val col = robot.pos.clone()
				col.addMultiple(dir, range)
				gc.beginPath()
				gc.moveTo(from.x, from.y)
				gc.lineTo(col.x, col.y)
				gc.stroke()
				gc.strokeOval(col.x - markerRadius, col.y - markerRadius, 2 * markerRadius, 2 * markerRadius)
			}

			val dirToGoal = maze.goal.clone()
			dirToGoal.sub(robot.pos)
			val angle = orientation angleTo dirToGoal
			control.pieGoalFinder.forEachIndexed { i, it ->
				if (it.contains(angle)) {
					val r = 30.0
					val a = -Angle(it.endInclusive.rad + robot.rot.rad).deg
					gc.fillArc(robot.pos.x - r, robot.pos.y - r, 2 * r, 2 * r, a, -90.0, ArcType.CHORD)
				}
			}
		}
	}

}
