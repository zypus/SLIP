package com.zypus.SLIP.models

open class SpringController(val angle: (SLIP) -> Double = { it.angle }, val constant: (SLIP) -> Double = {it.springConstant})
