package com.zypus.SLIP.models

data class SimulationSetting(val maxSimulationStep: Double = 0.1, val simulationStep: Double = maxSimulationStep, val epsilon: Double = 1e-4)