package com.zypus.SLIP.algorithms

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import com.zypus.utilities.Vector2

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 25/02/16
 */

class HillClimber : SpringControllerProvider {

	override fun createController(): SpringController {
		val initial = Initial(position = Vector2(0, 210))

		val setting = SimulationSetting()
		var factor1 = (Math.random() - 0.5) * 2 * 0.1
		var factor2 = (Math.random() - 0.5) * 2 * 0.1
		var maxScore = 0.0;
		val step1 = 0.05
		val step2 = 0.1
		for (t in 0..20) {
			var f1 = (Math.random() - 0.5) * 2 * 0.5
			var f2 = (Math.random() - 0.5) * 2 * 0.5
			var ms = 0.0;
			for (c in 0..2000) {
				val left1 = f1 - step1 * Math.random()
				val left2 = f2 - step2 * Math.random()
				val right1 = f1 + step1 * Math.random()
				val right2 = f2 + step2 * Math.random()
				val slip1 = SLIP(initial).copy(controller = SpringController { left1 * it.velocity.x + left2 })
				val slip2 = SLIP(initial).copy(controller = SpringController { right1 * it.velocity.x + right2 })
				val slip3 = SLIP(initial).copy(controller = SpringController { right1 * it.velocity.x + left2 })
				val slip4 = SLIP(initial).copy(controller = SpringController { left1 * it.velocity.x + right2 })
//				val environment = Environment(terrain = { 30.0 })
							val environment = Environment(terrain = { 40.0 + 20 * Math.sin(0.1 * it) })
				var state1 = SimulationState(slip1, environment)
				var state2 = SimulationState(slip2, environment)
				var state3 = SimulationState(slip3, environment)
				var state4 = SimulationState(slip4, environment)
				for (i in 0..4000) {
					if (Math.abs(state1.slip.angle) < 0.7) state1 = SimulationController.step(state1, setting)
					if (Math.abs(state2.slip.angle) < 0.7) state2 = SimulationController.step(state2, setting)
					if (Math.abs(state3.slip.angle) < 0.7) state3 = SimulationController.step(state3, setting)
					if (Math.abs(state4.slip.angle) < 0.7) state4 = SimulationController.step(state4, setting)
					if (Math.abs(state1.slip.angle) > 0.7 && Math.abs(state2.slip.angle) > 0.7 && Math.abs(state3.slip.angle) > 0.7  && Math.abs(state4.slip.angle) > 0.7 ) break
				}
				val score1 = if (Math.abs(state1.slip.angle) < 0.7) state1.slip.position.x else 0.1* state1.slip.position.x
				val score2 = if (Math.abs(state2.slip.angle) < 0.7) state2.slip.position.x else 0.1* state2.slip.position.x
				val score3 = if (Math.abs(state3.slip.angle) < 0.7) state3.slip.position.x else 0.1* state3.slip.position.x
				val score4 = if (Math.abs(state4.slip.angle) < 0.7) state4.slip.position.x else 0.1* state4.slip.position.x
				if (score1 > ms) {
					println(score1)
					f1 = left1
					f2 = left2
					ms = score1
				}
				if (score2 > ms) {
					println(score2)
					f1 = right1
					f2 = right2
					ms = score2
				}
				if (score3 > ms) {
					println(score3)
					f1 = right1
					f2 = left2
					ms = score3
				}
				if (score4 > ms) {
					println(score4)
					f1 = left1
					f2 = right2
					ms = score4
				}
			}
			if (ms > maxScore) {
				maxScore = ms
				factor1 = f1
				factor2 = f2
			}
		}
		println("Best factor: $factor1, $factor2")
		return SpringController { factor1 * it.velocity.x + factor2 }
	}

	// parameters to control - range
	// function to optimise

	fun hillClimb(params: Parameters, iterations: Int = 1, function: (Parameters) -> Double): Parameters {
		for (i in 1..iterations) {
			// Seed parameters
			// Test function
			// Find gradient
			// Update parameters
		}
		return Parameters()
	}


}

class Parameters {

}

