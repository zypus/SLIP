package com.zypus.SLIP.views

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
import com.zypus.gui.ResizableCanvas
import javafx.scene.layout.VBox
import org.reactfx.EventStreams
import org.reactfx.Subscription
import tornadofx.Fragment

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/04/16
 */
class SimpleStateFragment(var state: SimulationState, setting: SimulationSetting, width: Double = 500.0, height: Double = 500.0) : Fragment() {

	val canvas = ResizableCanvas(width, height)
	override val root = VBox(canvas)

	init {
		val gc = canvas.graphicsContext2D
		var s = state
		gc.drawSimulationState(s)

		var subscription: Subscription? = null
		subscription = EventStreams.animationFrames()
				.feedTo {
					if (!root.parent.scene.window.isShowing) {
						subscription!!.unsubscribe()
					} else {
						s = SimulationController.step(s, setting)
						gc.drawSimulationState(s)
						if (s.slip.crashed) s = state
					}
				}
	}

}