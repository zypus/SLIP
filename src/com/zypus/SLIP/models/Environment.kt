package com.zypus.SLIP.models

import com.zypus.math.Vector2

data class Environment(val gravity: Vector2 = Vector2(0, -9.8), val terrain: (Double) -> Double = {0.0})