package com.zypus.SLIP.views

import com.zypus.SLIP.algorithms.genetic.Entity
import com.zypus.SLIP.models.*
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import org.reactfx.EventStream
import org.reactfx.Subscription
import tornadofx.Fragment

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 03/05/16
 */
class ProgressFragment(val generation: EventStream<Int>, val bestSolution: ObjectProperty<Entity<*, *, *, *>>, val bestProblem: ObjectProperty<Entity<*, *, *, *>>): Fragment() {

	val vbox = VBox()
	override val root = ScrollPane(vbox)

	val initial = Initial()
	val setting = SimulationSetting()

	init {
		var subscription: Subscription? = null
		subscription = generation.feedTo {
			if (it != null && it % 100 == 0) {
				val solution = bestSolution.get()
				val problem = bestProblem.get()
				if (solution != null && problem != null) {
					if (solution.phenotype is SpringController) {
						val controller = solution.phenotype as SpringController
						val environment = problem.phenotype as Environment
						val s = SimulationState(SLIP(initial).copy(controller = controller), environment)
						Platform.runLater {
							vbox.children.add(0, SimpleStateFragment(s, setting, 500.0, 200.0).root)
							modalStage?.width = 500.0
							modalStage?.height = 800.0
						}
					} else if (solution.phenotype is SLIP) {
						val environment = problem.phenotype as Environment
						val s = SimulationState((solution.phenotype as SLIP).copy(position = initial.position, velocity = initial.velocity),environment)
						Platform.runLater {
							vbox.children.add(0, SimpleStateFragment(s, setting, 500.0, 200.0).root)
							modalStage?.width = 500.0
							modalStage?.height = 800.0
						}
					}

				}
			}
			if (!(root.scene?.window?.isShowing ?: true)) {
				subscription?.unsubscribe()
			}
		}
	}
}