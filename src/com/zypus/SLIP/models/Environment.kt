package com.zypus.SLIP.models

import com.zypus.SLIP.models.terrain.FlatTerrain
import com.zypus.SLIP.models.terrain.Terrain
import com.zypus.utilities.Vector2

data class Environment(val gravity: Vector2 = Vector2(0, -10.0), val terrain: Terrain = FlatTerrain(0.0))