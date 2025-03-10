package ui.navigation

sealed class Screen {
    object Home : Screen()
    object Favorites : Screen()
    object About : Screen()
    object Categories : Screen()
    object Tags : Screen()
} 