package com.zypus.SLIP.verification.benchmark

import com.zypus.SLIP.models.terrain.*
import java.io.File
import java.io.PrintWriter
import java.io.Writer
import java.util.*

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 15/05/16
 */

fun main(args: Array<String>) {
	/* Adding in most basic terrains. */
	val terrains: MutableList<Terrain> = arrayListOf(
			FlatTerrain(0.0),
			FlatTerrain(10.0),
			FlatTerrain(20.0),
			FlatTerrain(30.0),
			FlatTerrain(40.0)
	)

	val random = Random(123L)

	/* Add terrains with up to 5 sinus components. */
	for (c in 1..5) {
		for (t in 1..5) {
			terrains.add(CompositeTerrain(*(0..c).map {
				if (it == 0) {
					FlatTerrain(random.nextDouble() * 50)
				}
				else {
					SinusTerrain(
							frequency = 0.001 + random.nextDouble() * (0.2 - 0.001),
							amplitude = random.nextDouble() * 10,
							shift = random.nextDouble() * 2 * Math.PI
					)
				}
			}.toTypedArray()))
		}
	}

	val file = File("ShortTerrainBenchmark.txt")
	file.delete()
	val writer = file.printWriter()
	terrains.forEach { TerrainSerializer.serialize(writer, it) }
	writer.flush()

}

object TerrainSerializer {

	fun serialize(writer: Writer, terrain: Terrain) {
		val printWriter = PrintWriter(writer)
		with(printWriter) {
			when (terrain) {
				is FlatTerrain      -> {
					println("f ${terrain.height}")
				}
				is SinusTerrain     -> {
					println("s ${terrain.frequency} ${terrain.amplitude} ${terrain.shift} ${terrain.height}")
				}
				is CompositeTerrain -> {
					println("c ${terrain.components.size}")
					terrain.components.forEach { serialize(writer, it) }
				}
				is MidpointTerrain -> {
					println("m ${terrain.exp} ${terrain.height} ${terrain.roughness} ${terrain.displace}")
				}
			}
		}
		printWriter.flush()
	}

	fun deserialize(lines: MutableList<String>): Terrain? {
		if (!lines.isEmpty()) {
			val line = lines.removeAt(0).split(" ")
			return when (line[0]) {
				"f"  -> {
					FlatTerrain(line[1].toDouble())
				}
				"s"  -> {
					SinusTerrain(line[1].toDouble(), line[2].toDouble(), line[3].toDouble(), line[4].toDouble())
				}
				"c"  -> {
					CompositeTerrain(*(1..line[1].toInt()).map {
						deserialize(lines)!!
					}.toTypedArray())
				}
				"m" -> {
					MidpointTerrain(line[1].toInt(), line[2].toDouble(), line[3].toDouble(), line[4].toDouble())
				}
				else -> null
			}
		}
		else {
			return null
		}
	}

}
