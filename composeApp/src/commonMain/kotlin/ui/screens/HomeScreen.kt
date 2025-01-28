package ui.screens

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
import ui.components.*
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
    Box(modifier = Modifier.fillMaxSize()) {
        var searchText by remember { mutableStateOf("") }
        var showResults by remember { mutableStateOf(false) }
        var isAISearch by remember { mutableStateOf(false) }  // 添加搜索模式状态
        var hasAISearched by remember { mutableStateOf(false) }  // AI搜索状态标记
        
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
                            if (!isAISearch) {  // 普通搜索模式下实时搜索
                                if (it.isNotBlank()) {
                                    searchViewModel.search(it)
                                    showResults = true
                                } else {
                                    showResults = false
                                }
                            } else {  // AI搜索模式下，清空时重置状态
                                if (it.isBlank()) {
                                    hasAISearched = false
                                    showResults = false
                                }
                            }
                        },
                        label = { Text(if (isAISearch) "AI语义搜索" else "普通搜索") },
                        placeholder = { Text(if (isAISearch) "输入描述进行语义搜索..." else "输入关键词搜索...") },
                        leadingIcon = { 
                            Icon(Icons.Default.Search, "搜索")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 搜索模式切换按钮
                    TextButton(
                        onClick = { 
                            isAISearch = !isAISearch
                            searchText = ""  // 切换模式时清空搜索文本
                            showResults = false
                            hasAISearched = false
                        }
                    ) {
                        Text(if (isAISearch) "切换普通搜索" else "切换AI搜索")
                    }
                    
                    // AI搜索模式下显示搜索按钮
                    if (isAISearch) {
                        Button(
                            onClick = {
                                if (searchText.isNotBlank()) {
                                    aiSearchViewModel.searchInSystem(searchText)
                                    hasAISearched = true
                                    showResults = true
                                }
                            }
                        ) {
                            Text("搜索")
                        }
                    }
                }

                // 搜索结果或诗词列表
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isAISearch && aiSearchViewModel.isLoading.value -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        isAISearch && showResults && hasAISearched -> {
                            if (aiSearchViewModel.searchResults.value.isEmpty()) {
                                // AI搜索无结果显示
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("未找到相关诗词")
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(aiSearchViewModel.searchResults.value) { result ->
                                        AISearchResultCard(
                                            result = result,
                                            onAddToSystem = {}, // 系统内搜索不需要添加功能
                                            onClick = {}
                                        )
                                    }
                                }
                            }
                        }
                        !isAISearch && showResults -> {
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
                        }
                        else -> {
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
            }
            
            // 诗词详情
            Box(
                modifier = Modifier.weight(0.6f)
                    .fillMaxHeight()
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
                                Icon(Icons.Default.Edit, "编辑")
                            }
                            IconButton(onClick = { homeViewModel.onDeletePoemClick(poem) }) {
                                Icon(Icons.Default.Delete, "删除")
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