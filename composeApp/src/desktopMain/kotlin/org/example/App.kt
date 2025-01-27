package org.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import data.DatabaseManager
import data.DesktopDatabaseDriver
import data.PoemRepository
import ui.MainScreen
import viewmodel.ViewModelFactory

@Composable
fun App(
    deepseekApiKey: String,  // 从环境变量或配置文件获取
    deepseekBaseUrl: String = "https://api.deepseek.com"
) {
    val driver = remember { DesktopDatabaseDriver.create() }
    val databaseManager = remember { DatabaseManager(driver) }
    val repository = remember { PoemRepository(databaseManager) }
    val viewModelFactory = remember { ViewModelFactory(repository, deepseekApiKey, deepseekBaseUrl) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            MainScreen(viewModelFactory = viewModelFactory)
        }
    }
}