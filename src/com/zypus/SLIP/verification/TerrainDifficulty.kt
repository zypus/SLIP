package com.zypus.SLIP.verification

import com.zypus.SLIP.models.terrain.Terrain

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 09/05/16
 */
object TerrainDifficulty {

	fun meanHeight(terrain: Terrain): Double {
		return (0..999).sumByDouble { x -> terrain(x.toDouble()) }/1000.0
	}

	fun difficulty(terrain: Terrain): Double {
		val min = terrain((0..999).minBy { terrain(it.toDouble()) }!!.toDouble())
		val sum = (0..999).sumByDouble { terrain(it.toDouble()) }
		return (sum - 1000.0*min)/1000
	}

	fun spikiness(terrain: Terrain): Double {
		var lastY = terrain(-1.0)
		var ascending = true
		var min = 0.0
		var max = 0.0
		var spikiness = 0.0
		for (x in 0..999) {
			val y = terrain(x.toDouble())
			if (y > lastY) {
				if (!ascending) {
					spikiness += max - min
					ascending = true
				}
				max = y
			}
			else if (y < lastY) {
				if (ascending) {
					spikiness += max - min
					ascending = false
				}
				min = y
			}
			lastY = y
		}
		return spikiness
	}

	fun ascension(terrain: Terrain): Double {
		var lastY = terrain(-1.0)
		var ascending = true
		var min = 0.0
		var max = 0.0
		var length = 0
		var ascension = 0.0
		for (x in 0..999) {
			val y = terrain(x.toDouble())
			if (y > lastY) {
				max = y
				length++
				ascending = true
			}
			else if (y < lastY) {
				if (ascending) {
					ascension += (max - min)*length
					ascending = false
					length = 0
				}
				min = y
			}
			lastY = y
		}
		return ascension
	}

}

fun main(args: Array<String>) {
	println("DIFFICULTY")
	TestTerrains.terrains.forEach {
		val meanHeight = TerrainDifficulty.meanHeight(it)
		val spikiness = TerrainDifficulty.spikiness(it)
		val ascension = TerrainDifficulty.ascension(it)
		val total = meanHeight+spikiness+ascension
		println("${"%.4f %.4f %.4f %.4f".format(meanHeight, spikiness, ascension, total)} <- $it")
	}
}