package com.kapps.watson.presentation

import com.kapps.watson.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}