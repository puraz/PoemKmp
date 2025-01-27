package org.example

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "诗词收藏",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        App()
    }
}