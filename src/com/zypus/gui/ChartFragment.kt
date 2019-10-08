package com.zypus.gui

import javafx.scene.chart.Chart
import javafx.scene.layout.VBox
import tornadofx.Fragment

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/04/16
 */
class ChartFragment(val chart: Chart): Fragment() {

	override val root = VBox(chart)

}
