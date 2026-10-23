package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpaddesktop.app.AppBootstrap

/**
 * Main Entry Point for Desktop App.
 *
 * Bootstraps logging, headless driver execution, QoS timer resolution,
 * and launches the Compose Desktop application via [AppBootstrap].
 */
fun main(args: Array<String>) {
    AppBootstrap.start(args)
}
