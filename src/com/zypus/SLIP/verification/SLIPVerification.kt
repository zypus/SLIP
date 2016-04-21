package com.zypus.SLIP.verification

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.utilities.Vector2
import java.io.File

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 04/04/16
 */

fun main(args: Array<String>) {
	val statistic = Statistic("type", "initial x", "initial y", "initial vx", "initial vy", "initial angle", "rest length", "initial mass", "initial spring constant", "_compressed", "number of jumps", "collapsed", "max distance", "max height", "max velocity", "max angle", "min length", "end distance", "total time")

	val environment = Environment()
	val setting = SimulationSetting()
	val control = SpringController { SpringControl(-0.02014512293491862 * it.velocity.x + 0.13381311880313776, it.springConstant) }

	if (true) {

		val kSteps = 100
		val aSteps = 100

		for (t in arrayOf("uncontrolled", "controlled")) {
			for (k in 1.rangeTo(kSteps).map { it.toDouble() / kSteps * 20 }) {
				for (a in 0.rangeTo(aSteps).map { it.toDouble() / aSteps * Math.PI - Math.PI / 2 }) {
					val slip = SLIP(position = Vector2(0, 200), velocity = Vector2(30, 0), angle = a, springConstant = k, controller = if (t == "uncontrolled") SpringController { SpringControl(a,it.springConstant) } else control)
					var state = SimulationState(slip, environment)
					// initialize new statistic recording
					val row = statistic.newRow().apply {
						init(state, setting)
						this["type"] = t
					}
					// run the simulation until the slip collapsed or completed 50 jumps
					while (!(row.boolean("collapsed") ?: false) && row.int("number of jumps") ?: 0 < 50 ) {
						state = SimulationController.step(state, setting)
						row.update(state, setting)
					}
				}
			}
		}

		val file = File("comparison.csv")
		file.delete()
		val writer = file.printWriter()
		writer.print(statistic.toCSV())
		writer.flush()
		writer.close()

	} else {
		val heightSteps = 10;
		val velocitySteps = 10;
		val velocityStepSize = 20;

		val controller = mapOf("uncontrolled" to SpringController { SpringControl(it.angle, it.springConstant) }, "controlled" to control)

		for ((k, v) in controller) {
			for (y in 0.rangeTo(heightSteps).map { it.toDouble() / heightSteps * 200 + 110 }) {
				for (vx in 0.rangeTo(velocitySteps).map { it.toDouble() / velocitySteps * velocityStepSize }) {
					val slip = SLIP(position = Vector2(0, y), velocity = Vector2(vx, 0), controller = v)
					var state = SimulationState(slip, environment)
					// initialize new statistic recording
					val row = statistic.newRow().apply {
						init(state, setting)
						this["type"] = k
					}
					// run the simulation until the slip collapsed or completed 50 jumps
					while (!(row.boolean("collapsed") ?: false) && row.int("number of jumps") ?: 0 < 50 ) {
						state = SimulationController.step(state, setting)
						row.update(state, setting)
					}
				}
			}
		}

		val file = File("verification.csv")
		file.delete()
		val writer = file.printWriter()
		writer.print(statistic.toCSV())
		writer.flush()
		writer.close()
	}


}