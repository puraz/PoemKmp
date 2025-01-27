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
    val searchViewModel = remember { viewModelFactory.createSearchViewModel() }
    val homeViewModel = remember { viewModelFactory.createHomeViewModel() }
    
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧导航栏
            NavigationRail(
                modifier = Modifier.width(240.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colors.surface)
            ) {
                NavigationDrawerContent(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    searchViewModel = searchViewModel,
                    onPoemSelected = { poem -> 
                        homeViewModel.onPoemSelected(poem)
                    }
                )
            }
            
            // 主内容区
            Box(
                modifier = Modifier.weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen(
                        viewModel = homeViewModel
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