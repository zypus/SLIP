package com.zypus.SLIP.controllers

import com.zypus.SLIP.algorithms.genetic.EvolutionState
import com.zypus.SLIP.models.Statistic

/**
 * TODO Add description
 *
 * @author fabian <zypus@users.noreply.github.com>
 *
 * @created 10/05/16
 */
interface StatisticDelegate<SG : Any, SP : Any, SB : Any, SBC : Any, PG : Any, PP : Any, PB : Any, PBC : Any> {

	fun initialize(solutionCount: Int, problemCount: Int): Statistic

	fun update(stats: Statistic, generation: Int, state: EvolutionState<SG, SP, SB, SBC, PG, PP, PB, PBC>)

	fun save(stats: Statistic)

}