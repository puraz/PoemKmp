package org.example

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import theme.ThemeManager

fun main() = application {
    // 使用 Compose 的 isSystemInDarkTheme
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // 监听系统主题变化并更新 ThemeManager
    LaunchedEffect(systemInDarkTheme) {
        println("System is in dark theme: $systemInDarkTheme")
        ThemeManager.setSystemDarkTheme(systemInDarkTheme)
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "诗词收藏",
        state = rememberWindowState(size = DpSize(1130.dp, 800.dp))
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            App()
        }
    }
}