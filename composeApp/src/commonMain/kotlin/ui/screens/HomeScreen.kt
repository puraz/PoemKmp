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

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    aiSearchViewModel: AISearchViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        var searchText by remember { mutableStateOf("") }
        var showResults by remember { mutableStateOf(false) }
        
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                // 搜索栏
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { 
                        searchText = it
                        if (it.isNotBlank()) {
                            aiSearchViewModel.searchInSystem(it)
                            showResults = true
                        } else {
                            showResults = false
                        }
                    },
                    label = { Text("搜索系统内诗词") },
                    placeholder = { Text("输入关键词进行语义搜索...") },
                    leadingIcon = { 
                        Icon(Icons.Default.Search, "搜索")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // 搜索结果或诗词列表
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        aiSearchViewModel.isLoading.value -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        showResults && searchText.isNotBlank() -> {
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

fun HomeScreen(
    viewModel: AISearchViewModel,
    modifier: Modifier = Modifier
) {



}