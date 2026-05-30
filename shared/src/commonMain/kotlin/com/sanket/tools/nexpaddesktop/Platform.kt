package com.sanket.tools.nexpaddesktop

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform