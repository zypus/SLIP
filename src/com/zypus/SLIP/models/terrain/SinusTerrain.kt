package com.zypus.SLIP.models.terrain

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 21/04/16
 */
data class SinusTerrain(val frequency: Double, val amplitude: Double, val shift: Double = 0.0, val height: Double = 0.0): Terrain {

	override fun invoke(x: Double) = height + amplitude * Math.sin(shift + frequency * x)

	override fun toString(): String {
		return StringBuilder().apply {
			if (height != 0.0) append("%.4f ".format(height))
			if (amplitude != 0.0) {
				if (height != 0.0) append("+ ")
				if (amplitude != 1.0) append("%.4f ".format(amplitude))
				append("sin( ")
				if (shift != 0.0) append("%.4f + ".format(shift))
				if (frequency != 0.0) append("%.4f x".format(frequency))
				append(" )")
			}
		}.toString()
	}
}
