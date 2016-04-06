package com.zypus.SLIP.models

import java.lang.Math.*


/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 04/04/16
 */

class Statistic {

	val rows: MutableList<Row> = arrayListOf()
	var compressed = false
	private var initialized = false

	val current: Row?
		get() = if (initialized) rows.last() else null

	fun initialize(state: SimulationState, setting: SimulationSetting) {
		rows += Row(state.slip)
		initialized = true
	}

	fun update(state: SimulationState, setting: SimulationSetting) {
		assert(initialized)
		val currentRow = rows.last()
		val (position, velocity, angle, restLength, length, springConstant, mass, radius, standPosition, flightVelocity, headPosition, controller) = state.slip
		// determine if a jump was completed
		if (compressed && restLength == length) {
			compressed = false
			currentRow.numberOfJumps++
		} else if (restLength != length) {
			compressed = true
		}
		// figure out if the slip collapsed
		if (position.y-radius <= state.environment.terrain(position.x)) {
			currentRow.collapsed = true
		}
		// determine if max values were bested or min values
		currentRow.maxDistance = max(currentRow.maxDistance, abs(position.x))
		currentRow.maxHeight = max(currentRow.maxHeight, position.y)
		currentRow.maxVelocity = max(currentRow.maxVelocity, velocity.norm)
		currentRow.maxAngle = max(currentRow.maxAngle, abs(angle))
		currentRow.minLength = min(currentRow.minLength, length)
		// set the current distance as the end distance
		currentRow.endDistance = position.x
		// increment the past time
		currentRow.totalTime += setting.simulationStep
	}

	fun finalize(state: SimulationState, setting: SimulationSetting) {
		compressed = false
		initialized = false
	}

	class Row(slip: SLIP) {
		var ia = slip.angle
		var ik = slip.springConstant
		var ix = slip.position.x
		var iy = slip.position.y
		var ivx = slip.velocity.x
		var ivy = slip.velocity.y
		var ir = slip.radius
		var im = slip.mass
		var il = slip.restLength
		var numberOfJumps = 0
		var totalTime = 0.0
		var maxDistance = 0.0
		var maxHeight = 0.0
		var maxVelocity = 0.0
		var maxAngle = 0.0
		var minLength = slip.restLength
		var collapsed = false
		var endDistance = 0.0

		fun toCSV(): String {
			return "$ia, $ik, $ix, $iy, $ivx, $ivy, $ir, $im, $il, $numberOfJumps, $totalTime, $maxDistance, $maxHeight, $maxVelocity, $maxAngle, $minLength, ${if(collapsed) 1 else 0}, $endDistance"
		}
	}

	fun toCSV(): String {
		val builder = StringBuilder()
		builder.appendln("initial angle, initial spring constant, initial x, initial y, initial vx, initial vy, initial radius, initial mass, initial length, number of jumps, total time, max distance, max height, max velocity, max angle, min length, collapsed, end distance")
		rows.forEach { builder.appendln(it.toCSV()) }
		return builder.toString()
	}

}