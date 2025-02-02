package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.components.AddPoemFab
import ui.components.PoemDetail
import ui.components.PoemEditDialog
import ui.components.PoemListItem
import viewmodel.AISearchViewModel
import viewmodel.HomeViewModel
import viewmodel.SearchViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    aiSearchViewModel: AISearchViewModel,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colors // 获取当前主题颜色

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        var searchText by remember { mutableStateOf("") }
        var showResults by remember { mutableStateOf(false) }
        // var isAISearch by remember { mutableStateOf(false) }  // 注释AI搜索相关状态
        // var hasAISearched by remember { mutableStateOf(false) }
        
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                // 搜索栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { 
                            searchText = it
                            if (it.isNotBlank()) {
                                searchViewModel.search(it)
                                showResults = true
                            } else {
                                showResults = false
                            }
                        },
                        label = { Text("搜索") },
                        placeholder = { Text("输入关键词搜索...") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Search,
                                "搜索",
                                tint = colors.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = colors.onSurface,
                            placeholderColor = colors.onSurface.copy(alpha = 0.6f),
                            cursorColor = colors.primary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.onSurface.copy(alpha = 0.12f),
                            disabledTextColor = colors.onSurface.copy(alpha = 0.38f),
                            backgroundColor = colors.surface
                        )
                    )
                }

                // 搜索结果或诗词列表
                Box(
                    modifier = Modifier.weight(1f)
                        .background(colors.background)
                ) {
                    if (showResults) {
                        // 普通搜索结果
                        if (searchViewModel.searchResults.value.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("未找到相关诗词")
                            }
                        } else {
                            LazyColumn {
                                items(searchViewModel.searchResults.value) { poem ->
                                    PoemListItem(
                                        poem = poem,
                                        onClick = { homeViewModel.onPoemSelected(poem) }
                                    )
                                }
                            }
                        }
                    } else {
                        // 显示原有的诗词列表
                        LazyColumn {
                            items(homeViewModel.poems.value) { poem ->
                                PoemListItem(
                                    poem = poem,
                                    onClick = { homeViewModel.onPoemSelected(poem) }
                                )
                            }
                        }
                    }
                }
            }
            
            // 诗词详情
            Box(
                modifier = Modifier.weight(0.6f)
                    .fillMaxHeight()
                    .background(colors.background)
            ) {
                homeViewModel.selectedPoem.value?.let { poem ->
                    Column {
                        // 操作按钮
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { homeViewModel.onEditPoemClick(poem) }) {
                                Icon(
                                    Icons.Default.Edit,
                                    "编辑",
                                    tint = colors.onSurface
                                )
                            }
                            IconButton(onClick = { homeViewModel.onDeletePoemClick(poem) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    "删除",
                                    tint = colors.onSurface
                                )
                            }
                        }

                        PoemDetail(
                            poem = poem,
                            onFavoriteClick = { homeViewModel.toggleFavorite(poem) }
                        )
                    }
                }
            }
        }

        // 添加按钮
        AddPoemFab(
            onClick = homeViewModel::onAddPoemClick,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // 编辑对话框
        if (homeViewModel.showEditDialog.value) {
            PoemEditDialog(
                poem = homeViewModel.poemToEdit.value,
                onDismiss = homeViewModel::onEditDialogDismiss,
                onConfirm = homeViewModel::onEditDialogConfirm
            )
        }
    }
}