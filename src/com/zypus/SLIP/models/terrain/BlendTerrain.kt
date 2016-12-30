package com.zypus.SLIP.models.terrain

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 21/04/16
 */
class BlendTerrain(val first: Terrain, val second: Terrain) : Terrain {

	var blend = 0.0

	override fun invoke(x: Double): Double {
		val y = (1 - blend) * first.invoke(x) + blend * second.invoke(x)
		return y
	}

	override fun toString(): String = ""

	override fun equals(other: Any?): Boolean {
		return other is BlendTerrain && hashCode() == other.hashCode()
	}

	override fun hashCode(): Int {
		return first.hashCode() + second.hashCode()
	}

}