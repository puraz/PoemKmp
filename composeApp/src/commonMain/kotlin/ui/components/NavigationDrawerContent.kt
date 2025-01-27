package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.navigation.Screen

@Composable
fun NavigationDrawerContent(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 搜索框
        SearchBar(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // 导航项
        NavigationItem(
            icon = Icons.Default.Home,
            label = "首页",
            selected = currentScreen is Screen.Home,
            onClick = { onScreenSelected(Screen.Home) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        NavigationItem(
            icon = Icons.Default.Favorite,
            label = "收藏夹",
            selected = currentScreen is Screen.Favorites,
            onClick = { onScreenSelected(Screen.Favorites) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // NavigationItem(
        //     icon = Icons.Default.Menu,
        //     label = "分类",
        //     selected = currentScreen is Screen.Categories,
        //     onClick = { onScreenSelected(Screen.Categories) }
        // )
        //
        // Spacer(modifier = Modifier.height(8.dp))
        //
        // NavigationItem(
        //     icon = Icons.Default.AccountBox,
        //     label = "标签",
        //     selected = currentScreen is Screen.Tags,
        //     onClick = { onScreenSelected(Screen.Tags) }
        // )
        //
        // Spacer(modifier = Modifier.height(8.dp))
        //
        // NavigationItem(
        //     icon = Icons.Default.Settings,
        //     label = "设置",
        //     selected = currentScreen is Screen.Settings,
        //     onClick = { onScreenSelected(Screen.Settings) }
        // )
    }
}