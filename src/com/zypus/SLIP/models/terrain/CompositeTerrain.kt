package com.zypus.SLIP.models.terrain

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 21/04/16
 */
class CompositeTerrain(vararg val components: Terrain): Terrain {

	override fun invoke(x: Double) = components.sumByDouble { it(x) }

	override fun toString(): String = components.joinToString(separator = " + ")
}