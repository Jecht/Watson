package com.kapps.watson

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform