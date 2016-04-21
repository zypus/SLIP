package com.zypus.SLIP.models

import com.zypus.utilities.Vector2

/**
 * Models
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 14/02/16
 */

data class SLIP(val position: Vector2 = Vector2(0, 0), val velocity: Vector2 = Vector2(0, 0), val angle: Double = 0.0, val restLength: Double = 100.0, val length: Double = restLength, val springConstant: Double = 1.0, val mass: Double = 1.0, val radius: Double = 10.0, val standPosition: Vector2 = Vector2(0, 0), val controller: SpringController = SpringController { SpringControl(it.angle, it.springConstant) }) {

	constructor(initial: Initial): this(initial.position, initial.velocity, initial.angle)
}