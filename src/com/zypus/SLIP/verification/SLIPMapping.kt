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
 * @created 07/04/16
 */

fun main(args: Array<String>) {
	val statistic = Statistic()

	val environment = Environment()
	val setting = SimulationSetting()

	val angleStep = 200;
	val constantStep = 200;
	val constantStepSize = 1;

	for (a in 0.rangeTo(angleStep).map { it.toDouble() / angleStep * Math.PI - Math.PI/2 }) {
		for (b in 0.rangeTo(constantStep).map { it.toDouble() / constantStep * constantStepSize - constantStepSize.toDouble()/2 }) {
			val slip = SLIP(position = Vector2(0, 210), velocity = Vector2(0, 0), controller = SpringController { a*it.velocity.x+b })
			var state = SimulationState(slip, environment)
			// initialize new statistic recording
			statistic.initialize(state, setting, a = a, b = b)
			// run the simulation until the slip collapsed or completed 50 jumps
			while (!(statistic.current?.collapsed ?: true) && statistic.current?.numberOfJumps ?: 0 < 50 ) {
				state = SimulationController.step(state, setting)
				statistic.update(state, setting)
			}
			statistic.finalize(state, setting)
		}
	}

	val file = File("control.csv")
	file.delete()
	val writer = file.printWriter()
	writer.print(statistic.toCSV())
	writer.flush()
	writer.close()
}