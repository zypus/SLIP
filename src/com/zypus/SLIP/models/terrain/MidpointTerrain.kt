package com.zypus.SLIP.models.terrain

import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 22/05/16
 */
data class MidpointTerrain(val exp: Int, val height: Double, val roughness: Double, val displace: Double, val seed: Long = 0) : Terrain {

	val power = Math.pow(2.0, exp.toDouble()).toInt()
	val step = 1000.0 / power
	var leftSection = -1
	var rightSection = 1
	var leftPoints = generatePoints(-1, height, height, Array(power + 1) { 0.0 })
	var rightPoints = generatePoints(1, height, height, Array(power + 1) { 0.0 })


	override fun invoke(x: Double): Double {
		val section = x.toInt() / 1000 + if (x < 0) -1 else +1
		if (section < leftSection) {
			val temp = leftPoints
			leftPoints = generatePoints(section, height, height, rightPoints)
			rightPoints = temp
			rightSection = leftSection
			leftSection = section
		}
		else if (section > rightSection) {
			val temp = rightPoints
			rightPoints = generatePoints(section, height, height, leftPoints)
			leftPoints = temp
			leftSection = rightSection
			rightSection = section
		}
		val rawIndex = Math.abs((x % 1000.0) / step)
		val index = rawIndex.toInt()
		val t = rawIndex - Math.floor(rawIndex)
//		println("rawIndex = $rawIndex, index = $index, t = $t")
		if (section == leftSection) {
			return ((1.0 - t) * leftPoints[index] + t * leftPoints[index + 1]) / 2
		}
		else {
			return ((1.0 - t) * rightPoints[index] + t * rightPoints[index + 1]) / 2
		}
	}

	override fun toString(): String {
		return "$exp $height $roughness $displace $seed"
	}

	fun generatePoints(section: Int, left: Double, right: Double, points: Array<Double>): Array<Double> {
		val random = Random(section.toLong()+seed)
		points[0] = left
		points[power] = right

		var d = displace

		var i = 1
		while (i < power) {
			var j = (power / i) / 2
			while (j < power) {
				points[j] = ((points[j - (power / i) / 2] + points[j + (power / i) / 2]) / 2);
				points[j] += (random.nextDouble() * d * 2) - d
				// increment
				j += power / i
			}
			d *= roughness
			// increment
			i *= 2
		}
		return points
	}
}
