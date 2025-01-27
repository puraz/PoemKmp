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
import ui.navigation.Screen
import viewmodel.SearchViewModel
import viewmodel.AISearchViewModel
import data.db.Poem_entity

@Composable
fun NavigationDrawerContent(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    searchViewModel: SearchViewModel,
    aiSearchViewModel: AISearchViewModel,
    onPoemSelected: (Poem_entity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var showAISearch by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (showAISearch) {
            // AI 搜索面板
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部栏
                TopAppBar(
                    title = { Text("AI 智能搜索") },
                    navigationIcon = {
                        IconButton(onClick = { showAISearch = false }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp
                )
                
                // AI 搜索面板
                AISearchPanel(
                    viewModel = aiSearchViewModel,
                    onPoemSelected = { poem ->
                        onPoemSelected(poem)
                        showAISearch = false  // 选中后关闭搜索面板
                        onScreenSelected(Screen.Home)  // 切换到首页
                    }
                )
            }
        } else {
            // 常规导航菜单
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // AI 搜索入口
                Button(
                    onClick = { showAISearch = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "AI 搜索",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 智能搜索")
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
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}