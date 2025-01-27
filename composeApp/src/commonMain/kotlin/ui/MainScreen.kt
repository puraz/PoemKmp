package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.PoemRepository
import ui.components.NavigationDrawerContent
import ui.navigation.Screen
import ui.screens.*
import viewmodel.ViewModelFactory

@Composable
fun MainScreen(viewModelFactory: ViewModelFactory) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val homeViewModel = remember { viewModelFactory.createHomeViewModel() }
    // val searchViewModel = remember { viewModelFactory.createSearchViewModel() }
    val aiSearchViewModel = remember { viewModelFactory.createAISearchViewModel() }
    
    MaterialTheme {
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
                        homeViewModel = homeViewModel,
                        aiSearchViewModel = aiSearchViewModel
                    )
                    Screen.Favorites -> FavoritesScreen(
                        viewModel = remember { viewModelFactory.createFavoritesViewModel() }
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