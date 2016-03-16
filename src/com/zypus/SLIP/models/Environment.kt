package com.zypus.SLIP.models

import com.zypus.utilities.Vector2

data class Environment(val gravity: Vector2 = Vector2(0, -10.0), val terrain: (Double) -> Double = {0.0})