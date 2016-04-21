package com.zypus.SLIP.models.terrain

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 21/04/16
 */
class SinusTerrain(val frequency: Double, val amplitude: Double, val shift: Double = 0.0, val height: Double = 0.0): Terrain {

	override fun invoke(x: Double) = height + amplitude * Math.sin(shift + frequency * x)

}