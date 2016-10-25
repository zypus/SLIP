package com.zypus.Maze.view

import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.RangeFinder
import com.zypus.Maze.simulation.MazeNavigation
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.gui.ResizableCanvas
import com.zypus.utilities.Angle
import com.zypus.utilities.Vector2
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
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
class MazeFragment(var state: MazeNavigationState, setting: SimulationSetting, width: Double = 500.0, height: Double = 500.0) : Fragment() {

	val canvas = ResizableCanvas(width, height)
	override val root = VBox(canvas)

	init {
		val gc = canvas.graphicsContext2D
		var s = state
		drawState(gc,s)

		var subscription: Subscription? = null
		subscription = EventStreams.animationFrames()
				.feedTo {
					if (!root.parent.scene.window.isShowing) {
						subscription!!.unsubscribe()
					}
					else {
						s = MazeNavigation.step(s, setting)
						drawState(gc,s)
					}
				}
	}

	val markerRadius = 2.5
	val debug = true

	fun drawState(gc: GraphicsContext, state: MazeNavigationState) {

		gc.clearRect(0.0,0.0, canvas.width, canvas.height)

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

		gc.fillOval(start.x-markerRadius, start.y-markerRadius,2*markerRadius,2*markerRadius)
		gc.fillRect(goal.x-markerRadius, goal.y-markerRadius,2*markerRadius,2*markerRadius)

		// draw robot
		gc.strokeOval(
				robot.pos.x-robot.radius,
				robot.pos.y-robot.radius,
				2*robot.radius,
				2*robot.radius
				)

		gc.beginPath()
		val dir = robot.pos + Vector2(0,1).rotate(robot.rot) * robot.radius
		gc.moveTo(robot.pos.x, robot.pos.y)
				gc.lineTo(dir.x, dir.y)
		gc.stroke()

		if (debug) {
			gc.stroke = Color.GREEN
			gc.fill = Color.GREEN
			control.rangeFinders.map { // rotate the range finder by the rotation of the robot
				it.rotate(robot.rot)
			}.forEach { // compute the range to the nearest wall per range finder
				dir ->
				val range = RangeFinder.findRange(robot, maze, dir)
				val col = robot.pos + dir * range
				gc.strokeOval(col.x-markerRadius, col.y-markerRadius, 2*markerRadius, 2*markerRadius)
			}

			val dirToGoal = maze.goal - robot.pos
			val angle = (Vector2(0, 1).rotate(robot.rot)) angleTo dirToGoal
			control.pieGoalFinder.forEachIndexed { i, it ->
				if (angle in it) {
					val r = 30.0
					val a = -Angle(it.endInclusive.rad + robot.rot.rad).deg
					gc.fillArc(robot.pos.x- r,robot.pos.y- r,2* r,2* r, a,-90.0, ArcType.CHORD)
				}
			}
		}
	}

}