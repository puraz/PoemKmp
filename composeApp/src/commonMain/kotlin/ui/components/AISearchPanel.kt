package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.db.Poem_entity
import service.AISearchResult
import viewmodel.AISearchViewModel

@Composable
fun AISearchPanel(
    viewModel: AISearchViewModel,
    onPoemSelected: (Poem_entity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 搜索栏
        Surface(
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { 
                        searchText = it
                        viewModel.searchOutsideSystem(it)
                    },
                    label = { Text("AI 语义搜索") },
                    placeholder = { Text("例如：描写思乡的诗词、关于春天的诗...") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                
                // 搜索提示
                AnimatedVisibility(
                    visible = searchText.isNotBlank(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Text(
                        text = "AI 正在理解您的搜索意图...",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        
        // 搜索结果
        Box(modifier = Modifier.weight(1f)) {
            when {
                viewModel.isLoading.value -> {
                    // 加载动画
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在搜索相关诗词...",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                searchText.isBlank() -> {
                    // 空状态提示
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "输入关键词开始搜索",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI 将理解您的搜索意图，找到最相关的诗词",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                viewModel.searchResults.value.isEmpty() -> {
                    // 无结果提示
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "未找到相关诗词",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "试试换个关键词？",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                else -> {
                    // 搜索结果列表
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.searchResults.value) { result ->
                            AISearchResultCard(
                                result = result,
                                onAddToSystem = { 
                                    viewModel.convertToSystemPoem(result)
                                },
                                onClick = {
                                    // onPoemSelected(result.toSystemPoem())
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

