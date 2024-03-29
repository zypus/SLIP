package com.zypus.SLIP.verification

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.*
import mikera.vectorz.Vector2
import golem.max
import java.lang.Math.*
import kotlin.system.measureTimeMillis


/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 11/04/16
 */

fun Statistic.Row.init(state: SimulationState, setting: SimulationSetting) {
	val (position, velocity, angle, restLength, length, springConstant, mass, radius, standPosition, controller) = state.slip
	this["initial x"] = position.x
	this["initial y"] = position.y
	this["initial vx"] = velocity.x
	this["initial vy"] = velocity.y
	this["initial angle"] = angle
	this["rest length"] = restLength
	this["initial spring constant"] = springConstant
	this["initial mass"] = mass
}

fun Statistic.Row.update(state: SimulationState, setting: SimulationSetting) {
	val (position, velocity, angle, restLength, length, springConstant, mass, radius, standPosition, controller) = state.slip
	// determine if a jump was completed
	if (boolean("_compressed") ?: false && restLength == length) {
		this["_compressed"] = false
		this["number of jumps"] = 1 + (int("number of jumps")?:0)
	}
	else if (restLength != length) {
		this["_compressed"] = true
	}
	// figure out if the slip collapsed
	if (position.y - radius <= state.environment.terrain(position.x)) {
		this["collapsed"] = true
	}
	// determine if max values were bested or min values
	this["max distance"] = max(double("max distance")?:0.0, abs(position.x))
	this["max height"] = max(double("max height") ?: 0.0, position.y)
	this["max velocity"] = max(double("max velocity") ?: 0.0, velocity.magnitude())
	this["max angle"] = max(double("max angle") ?: 0.0, abs(angle))
	this["min length"] = min(double("min length") ?: 0.0, length)
	// set the current distance as the end distance
	this["end distance"] = position.x
	// increment the past time
	this["total time"] = double("total time")?:0.0 + setting.simulationStep
}

fun main(args: Array<String>) {
	var state = SimulationState(SLIP(position = Vector2(210.0, 0.0)), Environment())
	val setting = SimulationSetting()
	val millis = measureTimeMillis {
		for (i in 1..10000) {
			state = SimulationController.step(state, setting)
		}
	}
	println(millis)
}
