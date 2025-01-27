import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRail
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.PoemRepository
import ui.screens.HomeScreen

@Composable
fun MainScreen(repository: PoemRepository) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    
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
                    onScreenSelected = { currentScreen = it }
                )
            }
            
            // 主内容区
            Box(
                modifier = Modifier.weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen(repository = repository)
                    Screen.Categories -> TODO()
                    Screen.Favorites -> TODO()
                    Screen.Settings -> TODO()
                    Screen.Tags -> TODO()
                }
            }
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object Favorites : Screen() 
    object Categories : Screen()
    object Tags : Screen()
    object Settings : Screen()
} 