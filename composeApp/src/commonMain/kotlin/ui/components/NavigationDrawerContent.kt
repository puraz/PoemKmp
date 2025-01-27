import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import ui.components.SearchBar

@Composable
fun NavigationDrawerContent(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    ) {
        // 搜索框
        SearchBar(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        //
        // // 导航项
        // NavigationItem(
        //     icon = Icons.Default.Home,
        //     label = "首页",
        //     selected = currentScreen is Screen.Home,
        //     onClick = { onScreenSelected(Screen.Home) }
        // )
        //
        // NavigationItem(
        //     icon = Icons.Default.Favorite,
        //     label = "收藏夹",
        //     selected = currentScreen is Screen.Favorites,
        //     onClick = { onScreenSelected(Screen.Favorites) }
        // )
        
        // NavigationItem(
        //     icon = Icons.Default.Category,
        //     label = "分类",
        //     selected = currentScreen is Screen.Categories,
        //     onClick = { onScreenSelected(Screen.Categories) }
        // )
        //
        // NavigationItem(
        //     icon = Icons.Default.Tag,
        //     label = "标签",
        //     selected = currentScreen is Screen.Tags,
        //     onClick = { onScreenSelected(Screen.Tags) }
        // )
        
        // NavigationItem(
        //     icon = Icons.Default.Settings,
        //     label = "设置",
        //     selected = currentScreen is Screen.Settings,
        //     onClick = { onScreenSelected(Screen.Settings) }
        // )
    }
}