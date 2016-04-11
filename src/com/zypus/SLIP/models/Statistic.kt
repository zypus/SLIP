package com.zypus.SLIP.models

import java.lang.Math.*


/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 04/04/16
 */

class Statistic() {

	val rows: MutableList<Row> = arrayListOf()
	var compressed = false
	private var initialized = false

	val current: Row?
		get() = if (initialized) rows.last() else null

	fun initialize(state: SimulationState, setting: SimulationSetting, type: String = "unspecified", a: Double? = null, b: Double? = null, c: Double? = null, d: Double? = null, e: Double? = null) {
		val row = Row(state.slip)
		row.type = type
		// Custom values
		if (a != null) row.a = a
		if (b != null) row.b = b
		if (c != null) row.c = c
		if (d != null) row.d = d
		if (e != null) row.e = e
		rows += row
		initialized = true
	}

	fun update(state: SimulationState, setting: SimulationSetting, a:Double? = null, b: Double? = null, c: Double? = null, d: Double? = null, e: Double? = null) {
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
		// Custom values
		if (a != null) currentRow.a = a
		if (b != null) currentRow.b = b
		if (c != null) currentRow.c = c
		if (d != null) currentRow.d = d
		if (e != null) currentRow.e = e
	}

	fun finalize(state: SimulationState, setting: SimulationSetting) {
		compressed = false
		initialized = false
	}

	class Row(slip: SLIP) {
		var type = "unspecified"
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
		var a = 0.0
		var b = 0.0
		var c = 0.0
		var d = 0.0
		var e = 0.0

		fun toCSV(): String {
			return "$type, $ia, $ik, $ix, $iy, $ivx, $ivy, $ir, $im, $il, $numberOfJumps, $totalTime, $maxDistance, $maxHeight, $maxVelocity, $maxAngle, $minLength, ${if(collapsed) 1 else 0}, $endDistance, $a, $b, $c, $d, $e"
		}
	}

	fun toCSV(): String {
		val builder = StringBuilder()
		builder.appendln("type, initial angle, initial spring constant, initial x, initial y, initial vx, initial vy, initial radius, initial mass, initial length, number of jumps, total time, max distance, max height, max velocity, max angle, min length, collapsed, end distance, a, b, c, d, e")
		rows.forEach { builder.appendln(it.toCSV()) }
		return builder.toString()
	}

}