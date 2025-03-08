package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRail
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.DatabaseManager
import manager.AIModelManager
import theme.ThemeManager
import ui.components.NavigationDrawerContent
import ui.navigation.Screen
import ui.screens.FavoritesScreen
import ui.screens.HomeScreen
import viewmodel.ViewModelFactory

@Composable
fun MainScreen(viewModelFactory: ViewModelFactory, databaseManager: DatabaseManager) {
    // 初始化 AIModelManager
    AIModelManager.initialize(databaseManager)
    ThemeManager.initialize(databaseManager)

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val aiSearchViewModel = remember { viewModelFactory.createAISearchViewModel() }
    val isDarkTheme = ThemeManager.isDarkTheme
    
    MaterialTheme(
        colors = if (isDarkTheme) darkColors() else lightColors()
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier
                    .width(320.dp)  // 增加宽度以适应 AI 搜索面板
                    .fillMaxHeight()
            ) {
                NavigationDrawerContent(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    aiSearchViewModel = aiSearchViewModel,
                )
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen(
                        viewModelFactory = viewModelFactory
                    )
                    Screen.Favorites -> FavoritesScreen(
                        viewModelFactory = viewModelFactory
                    )
                    // Screen.Categories -> CategoriesScreen()
                    // Screen.Tags -> TagsScreen()
                    // Screen.Settings -> SettingsScreen()
                    Screen.Categories -> TODO()
                    Screen.Settings -> TODO()
                    Screen.Tags -> TODO()
                }
            }
        }
    }
}