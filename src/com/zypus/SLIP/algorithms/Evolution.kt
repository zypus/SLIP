package com.zypus.SLIP.algorithms

import com.zypus.SLIP.algorithms.genetic.Entity
import javafx.beans.property.ObjectProperty

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 19/04/16
 */
interface Evolution {

	fun solutionsProperty(): ObjectProperty<List<Entity<*, *, *>>>
	fun generationProperty(): ObjectProperty<Int>
	fun progressProperty(): ObjectProperty<Double>
	fun finishedProperty(): ObjectProperty<Boolean>

}