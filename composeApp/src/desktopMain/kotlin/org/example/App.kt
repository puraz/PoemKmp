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

@Composable
fun App() {
    val driver = remember { DesktopDatabaseDriver.create() }
    val databaseManager = remember { DatabaseManager(driver) }
    val repository = remember { PoemRepository(databaseManager) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            MainScreen(repository = repository)
        }
    }
}