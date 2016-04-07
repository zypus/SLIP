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
	val statistic = Statistic()

	val environment = Environment()
	val setting = SimulationSetting()

	val heightSteps = 10;
	val velocitySteps = 10;
	val velocityStepSize = 20;
//	for( a in 0.rangeTo(heightSteps).map { it.toDouble()/heightSteps * Math.PI - 0.5*Math.PI }) {
//		for (k in 1.rangeTo(velocitySteps).map { it.toDouble()/velocitySteps * velocityStepSize }) {
//			val slip = SLIP(angle = a, springConstant = k, position = Vector2(0, 210), velocity = Vector2(0,0), controller = controller)
//			var state = SimulationState(slip, environment)
//			// initialize new statistic recording
//			statistic.initialize(state, setting)
//			// run the simulation until the slip collapsed or completed 50 jumps
//			while(!(statistic.current?.collapsed ?: true) && statistic.current?.numberOfJumps?:0 < 50 ) {
//				state = SimulationController.step(state, setting)
//				statistic.update(state, setting)
//			}
//			statistic.finalize(state, setting)
//		}
//	}
	val controller = mapOf("uncontrolled" to SpringController { it.angle }, "controlled" to SpringController { -0.02014512293491862 * it.velocity.x + 0.13381311880313776 })

	for ((k,v) in controller) {
		for (y in 0.rangeTo(heightSteps).map { it.toDouble() / heightSteps * 200 + 110 }) {
			for (vx in 0.rangeTo(velocitySteps).map { it.toDouble() / velocitySteps * velocityStepSize }) {
				val slip = SLIP(position = Vector2(0, y), velocity = Vector2(vx, 0), controller = v)
				var state = SimulationState(slip, environment)
				// initialize new statistic recording
				statistic.initialize(state, setting, k)
				// run the simulation until the slip collapsed or completed 50 jumps
				while (!(statistic.current?.collapsed ?: true) && statistic.current?.numberOfJumps ?: 0 < 50 ) {
					state = SimulationController.step(state, setting)
					statistic.update(state, setting)
				}
				statistic.finalize(state, setting)
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