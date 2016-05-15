package com.zypus.SLIP.models.terrain

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 21/04/16
 */
class FlatTerrain(val height: Double): Terrain {

	override fun invoke(x: Double): Double = height

	override fun toString(): String = "%.4f".format(height)
}