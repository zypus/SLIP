package com.zypus.SLIP.verification

import com.zypus.SLIP.models.terrain.CompositeTerrain
import com.zypus.SLIP.models.terrain.FlatTerrain
import com.zypus.SLIP.models.terrain.SinusTerrain

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 06/05/16
 */
object TestTerrains {

	val terrains by lazy {
		arrayListOf(
				FlatTerrain(0.0),
				FlatTerrain(10.0),
				FlatTerrain(20.0),
				FlatTerrain(30.0),
				FlatTerrain(40.0),
				FlatTerrain(50.0),
				SinusTerrain(0.05, 10.0, 0.0, 30.0),
				SinusTerrain(0.05, 20.0, 0.0, 30.0),
				SinusTerrain(0.1, 10.0, 0.0, 30.0),
				SinusTerrain(0.2, 10.0, 0.0, 30.0),
				SinusTerrain(0.2, 5.0, 0.0, 30.0),
				SinusTerrain(0.3, 10.0, 0.0, 30.0),
				SinusTerrain(0.3, 5.0, 0.0, 30.0),
				CompositeTerrain(
						SinusTerrain(0.05, 20.0, 0.0, 30.0),
						SinusTerrain(0.1, 10.0, 1.0, 0.0)
				),
				CompositeTerrain(
						SinusTerrain(0.01, 20.0, 0.0, 30.0),
						SinusTerrain(0.2, 5.0, 1.0, 0.0)
				),
				CompositeTerrain(
						SinusTerrain(0.1, 10.0, 0.0, 30.0),
						SinusTerrain(0.1, 10.0, 1.0, 0.0)
				),
				CompositeTerrain(
						SinusTerrain(0.05, 20.0, 0.0, 30.0),
						SinusTerrain(0.1, 10.0, 1.0, 0.0),
						SinusTerrain(0.2, 5.0, 2.0, 0.0)
				),
				CompositeTerrain(
						SinusTerrain(0.01, 30.0, 0.0, 30.0),
						SinusTerrain(0.1, 10.0, 1.0, 0.0),
						SinusTerrain(0.3, 3.0, 2.0, 0.0)
				),
				CompositeTerrain(
						SinusTerrain(0.05, 20.0, 0.0, 30.0),
						SinusTerrain(0.1, 5.0, 1.0, 0.0),
						SinusTerrain(0.2, 10.0, 2.0, 0.0),
						SinusTerrain(0.2, 5.0, 3.0, 0.0),
						SinusTerrain(0.2, 15.0, 4.0, 0.0)
				),
				CompositeTerrain(
						SinusTerrain(0.05, 20.0, 0.0, 30.0),
						SinusTerrain(0.05, 5.0, 1.0, 0.0),
						SinusTerrain(0.05, 10.0, 2.0, 0.0),
						SinusTerrain(0.05, 5.0, 3.0, 0.0),
						SinusTerrain(0.05, 15.0, 4.0, 0.0)
				)

		).sortedBy {
			val meanHeight = TerrainDifficulty.meanHeight(it)
			val spikiness = TerrainDifficulty.spikiness(it)
			val ascension = TerrainDifficulty.ascension(it)
			meanHeight + spikiness + ascension
		}
	}

}