package com.zypus.SLIP.models.terrain

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 21/04/16
 */
interface Terrain {

	operator fun invoke(x: Double): Double

	override fun toString(): String

}