package com.zypus.SLIP.views

import com.zypus.SLIP.controllers.SimulationController
import com.zypus.SLIP.models.SimulationSetting
import com.zypus.SLIP.models.SimulationState
import com.zypus.gui.ResizableCanvas
import javafx.scene.layout.VBox
import org.reactfx.EventStreams
import tornadofx.Fragment

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 23/04/16
 */
class SimpleStateFragment(var state: SimulationState, setting: SimulationSetting) : Fragment() {

	val canvas = ResizableCanvas(500.0, 500.0)
	override val root = VBox()

	init {
		val gc = canvas.graphicsContext2D
		var s = state
		gc.drawSimulationState(s)

		EventStreams.animationFrames()
				.feedTo {
					s = SimulationController.step(s, setting)
					gc.drawSimulationState(s)
					if (s.slip.position.y - s.slip.radius <= s.environment.terrain(s.slip.position.x)) s = state
				}
	}

}