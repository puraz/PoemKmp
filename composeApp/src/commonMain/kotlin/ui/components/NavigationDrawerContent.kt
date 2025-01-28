package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ui.navigation.Screen
import viewmodel.SearchViewModel
import viewmodel.AISearchViewModel
import data.db.Poem_entity

@Composable
fun NavigationDrawerContent(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    aiSearchViewModel: AISearchViewModel,
    modifier: Modifier = Modifier
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 发现新诗词按钮
        Button(
            onClick = { showSearchDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "发现新诗词",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("发现新诗词")
        }
        
        // 导航菜单项
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
    }

    // 搜索对话框
    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("发现新诗词") },
            text = {
                Box(modifier = Modifier.size(800.dp, 600.dp)) {
                    AISearchPanel(
                        viewModel = aiSearchViewModel,
                        onPoemSelected = { /* 暂时不需要处理 */ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
}