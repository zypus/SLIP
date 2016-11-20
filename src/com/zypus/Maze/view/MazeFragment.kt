package com.zypus.Maze.view

import com.zypus.Maze.controller.DeepRnnRobotController
import com.zypus.Maze.models.Maze
import com.zypus.Maze.models.MazeNavigationState
import com.zypus.Maze.models.RangeFinder
import com.zypus.Maze.simulation.MazeNavigation
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.gui.ResizableCanvas
import com.zypus.utilities.Angle
import com.zypus.utilities.LineSegment
import com.zypus.utilities.angleTo
import com.zypus.utilities.rotate
import javafx.scene.Parent
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import javafx.stage.Modality
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
class MazeFragment(val parent: Parent, var state: MazeNavigationState, setting: SimulationSetting, width: Double = 500.0, height: Double = 500.0) : Fragment() {

	val canvas = ResizableCanvas(width, height)
	override val root = VBox(canvas)

	val maze1 = {
		val walls = arrayListOf(
				LineSegment(Vector2(0.0, 0.0), Vector2(0.0, 500.0)),
				LineSegment(Vector2(0.0, 500.0), Vector2(500.0, 500.0)),
				LineSegment(Vector2(0.0, 0.0), Vector2(500.0, 0.0)),
				LineSegment(Vector2(500.0, 0.0), Vector2(500.0, 500.0)),
				LineSegment(Vector2(100.0, 400.0), Vector2(400.0, 100.0))
		)

		val start = Vector2(100.0, 100.0)
		val goal = Vector2(400.0, 400.0)

		Maze(walls, start, goal)
	}()

	init {
		val gc = canvas.graphicsContext2D
		var controller = state.controller.copy()
		controller.start()

		var maze = maze1

		var s = MazeNavigationState(state.robot, maze, controller)
		drawState(gc,s)

		var count = 0

		val rnnVisu = LiveRnnFragment()
		rnnVisu.openModal(modality = Modality.NONE)

		var subscription: Subscription? = null
		subscription = EventStreams.animationTicks()
				.feedTo {
					canvas.requestFocus()
					if (!root.parent.scene.window.isShowing) {
						subscription!!.unsubscribe()
					}
					else {
						s = MazeNavigation.step(s, setting)
						drawState(gc,s)
						rnnVisu.update((controller as DeepRnnRobotController).rnn!!, controller.latestInputs.dropLast(1), controller.latestOutput)
						count++
					}
				}

		with(canvas) {
			setOnKeyTyped {
				if (it.character == "r") {
					count = 0
					controller = state.controller.copy()
					controller.start()
					s = MazeNavigationState(state.robot.copy(maze.start.clone()), maze, controller)
				} else if (it.character in "123") {
					count = 0
					controller = state.controller.copy()
					controller.start()
					maze = when (it.character) {
						"1" -> maze1
						"2" -> maze2
						"3" -> maze3
						else -> maze1
					}
					s = MazeNavigationState(state.robot.copy(maze.start.clone()), maze, controller)
				}
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
		val dir = robot.pos.clone()
		val orientation = mikera.vectorz.Vector2(0.0, 1.0)
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

	val maze2 by lazy {
		val walls = arrayListOf(
				LineSegment(Vector2(0.0, 0.0), Vector2(0.0, 500.0)),
				LineSegment(Vector2(0.0, 500.0), Vector2(500.0, 500.0)),
				LineSegment(Vector2(0.0, 0.0), Vector2(500.0, 0.0)),
				LineSegment(Vector2(500.0, 0.0), Vector2(500.0, 500.0)),
				LineSegment(Vector2(50.0, 250.0), Vector2(450.0, 250.0))
		)

		val start = Vector2(100.0, 100.0)
		val goal = Vector2(400.0, 400.0)

		Maze(walls, start, goal)
	}

	val maze3 by lazy {
		val walls = arrayListOf(
				LineSegment(Vector2(0.0, 0.0), Vector2(0.0, 500.0)),
				LineSegment(Vector2(0.0, 500.0), Vector2(500.0, 500.0)),
				LineSegment(Vector2(0.0, 0.0), Vector2(500.0, 0.0)),
				LineSegment(Vector2(500.0, 0.0), Vector2(500.0, 500.0)),
				LineSegment(Vector2(250.0, 50.0), Vector2(250.0, 400.0))
		)

		val start = Vector2(50.0, 250.0)
		val goal = Vector2(400.0, 250.0)

		Maze(walls, start, goal)
	}

}