package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.navigation.Screen
import viewmodel.SearchViewModel
import data.db.Poem_entity

@Composable
fun NavigationDrawerContent(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    searchViewModel: SearchViewModel,
    onPoemSelected: (Poem_entity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // 搜索框
        OutlinedTextField(
            value = searchText,
            onValueChange = { 
                searchText = it
                searchViewModel.search(it)
            },
            label = { Text("搜索") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        //     icon = Icons.Default.List,  // 使用 List 替代 Category
        //     label = "分类",
        //     selected = currentScreen is Screen.Categories,
        //     onClick = { onScreenSelected(Screen.Categories) }
        // )
        //
        // Spacer(modifier = Modifier.height(8.dp))
        //
        // NavigationItem(
        //     icon = Icons.Default.Label,  // 使用 Label 替代 Tag
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
        //
        // 搜索结果
        if (searchText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "搜索结果",
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (searchViewModel.isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                        .padding(8.dp)
                )
            } else {
                LazyColumn {
                    items(searchViewModel.searchResults.value) { poem ->
                        SearchResultItem(
                            poem = poem,
                            onClick = {
                                onPoemSelected(poem)
                                onScreenSelected(Screen.Home)
                            }
                        )
                    }
                }
            }
        }
    }
}