package com.zypus.results

import java.io.File

/**
 * TODO Add description.
 *
 * @author Fabian <zypus@users.noreply.github.com>
 * @since 29/5/2016
 * @version 1.0
 */

fun main(args: Array<String>) {

    val runs = 10
    val indis = 50
    val expName = "seed4000"

    val writer = File("benchedExperiments/$expName/data.csv").printWriter()

    // Write out header
    val combinedHeader = arrayListOf("algorithm", "cycle", "replication", "entity", "slip.fitness", "a", "b", "c", "d", "l", "m", "slip.benchmark.stability", "slip.benchmark.distance", "terrain.fitness", "height", "power", "roughness", "displacement", "spikiness", "ascension", "terrain.benchmark.stability", "terrain.benchmark.distance", "difficulty")
    writer.println(combinedHeader.joinToString(separator = ","))

    val algorithms = arrayListOf("terrain.diversity", "slip.diversity", "both.fitness", "both.fitness.adaptive", "both.diversity")
    for (filename in algorithms) {
        for (r in 1..runs) {
            val reader = File("benchedExperiments/$expName/$filename$r.csv").bufferedReader()
            val header = reader.readLine().split(",")
            var cycle = 0
            reader.forEachLine { line ->
                val entries = line.split(",")
                val labeled = header.zip(entries)
                for (i in 0..indis) {
                    val row = mutableListOf("$filename", "$cycle", "$r", "$i")
                    val sd = "s$i "
                    val pd = "p$i "
                    labeled.forEach {
                        if (it.first.contains(sd) || it.first.contains(pd)) {
                            row += it.second
                        }
                    }
                    writer.println(row.joinToString(separator = ","))
                }
                cycle++
            }
            println("$filename: file $r/$runs completed")
        }
    }
    writer.flush()

}
