package com.zypus.Maze.view

import com.zypus.rnn.DeepRnn
import net.sourceforge.plantuml.SourceStringReader
import tornadofx.Fragment
import tornadofx.imageview
import tornadofx.vbox
import java.io.File

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 19/11/2016
 */
class RnnFragment(val rnn: DeepRnn): Fragment() {
	override val root = vbox {
		val diagram = rnn.toDiagram()
		val reader = SourceStringReader(diagram)

		val file = File("diagram.png")
		reader.generateImage(file)

		imageview(file.toURI().toURL().toString())
	}
}

fun DeepRnn.toDiagram(): String {
	return StringBuilder().apply {
		appendln("@startuml\n")
		hiddenLayers.forEachIndexed { l, hidden ->
			appendln("state layer_$l {")
			(1..hidden.Wxh.columnCount()).forEach {
				appendln("state N${l}_$it")
			}
			appendln("}")
		}
		val y = hiddenLayers.size + 1
		appendln("state layer_${y-1} {")
		(1..Why.columnCount()).forEach {
			appendln("state N${y - 1}_$it")
		}
		appendln("}")
		appendln("state output {")
		(1..Why.rowCount()).forEach {
			appendln("state N${y}_$it")
		}
		appendln("}")

		hiddenLayers.forEachIndexed { l, hidden ->
			hidden.bh.forEachIndexed { j, d ->
				if (d != 0.0) {
					appendln("Bias$l -> N${l + 1}_${j+1}")
				}
			}
			hidden.Wxh.forEachIndexed { i, aVector ->
				aVector.forEachIndexed { j, d ->
					if (d != 0.0) {
						appendln("N${l}_${j+1} -> N${l + 1}_${i + 1}")
					}
				}
			}
			hidden.Whh.forEachIndexed { i, aVector ->
				aVector.forEachIndexed { j, d ->
					if (d != 0.0) {
						appendln("N${l+1}_${j + 1} -> N${l + 1}_${i+1}")
					}
				}
			}
		}
		by.forEachIndexed { j, d ->
			if (d != 0.0) {
				appendln("Bias${y - 1} -> N${y}_${j+1}")
			}
		}
		Why.forEachIndexed { i, aVector ->
			aVector.forEachIndexed { j, d ->
				if (d != 0.0) {
					appendln("N${y-1}_${j+1} -> N${y}_${i+1}")
				}
			}
		}
		appendln("\n@enduml")
	}.toString()
}