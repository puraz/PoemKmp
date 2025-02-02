package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.db.Poem_entity
import kotlinx.coroutines.delay
import viewmodel.AISearchViewModel

@Composable
fun AISearchPanel(
    viewModel: AISearchViewModel,
    onPoemSelected: (Poem_entity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var showAddSuccessSnackbar by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            // .padding(8.dp)  // 添加整体内边距
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 搜索栏 - 使用 Surface 固定在顶部
            Surface(
                elevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()  // 高度适应内容
            ) {
                Column {
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
                                if (it.isBlank()) {
                                    viewModel.clearSearchResults()
                                }
                            },
                            label = { Text("搜索新诗词") },
                            placeholder = { Text("例如：描写思乡的诗词、关于春天的诗...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !viewModel.isLoading.value,  // 搜索时禁用输入
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchText.isNotBlank() && !viewModel.isLoading.value) {
                                        hasSearched = true
                                        viewModel.searchOutsideSystem(searchText)
                                    }
                                }
                            )
                        )

                        Button(
                            onClick = {
                                if (searchText.isNotBlank()) {
                                    hasSearched = true
                                    viewModel.searchOutsideSystem(searchText)
                                }
                            },
                            enabled = searchText.isNotBlank() && !viewModel.isLoading.value  // 搜索时禁用按钮
                        ) {
                            Text("搜索")
                        }
                    }
                    
                    // 搜索提示
                    AnimatedVisibility(
                        visible = viewModel.isLoading.value,
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

            // 搜索结果 - 使用固定高度的容器
            Box(
                modifier = Modifier
                    .weight(1f)  // 占用剩余空间
                    .fillMaxWidth()
            ) {
                when {
                    viewModel.isLoading.value -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
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
                    }
                    searchText.isBlank() || !hasSearched -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "输入关键词开始搜索",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    viewModel.searchResults.value.isEmpty() && hasSearched -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "未找到相关诗词",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(viewModel.searchResults.value) { result ->
                                AISearchResultCard(
                                    result = result,
                                    onAddToSystem = { 
                                        viewModel.convertToSystemPoem(result)
                                        showAddSuccessSnackbar = true
                                    },
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Snackbar
        if (showAddSuccessSnackbar) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                backgroundColor = MaterialTheme.colors.surface,  // 使用主题的 surface 颜色
                contentColor = MaterialTheme.colors.onSurface,  // 使用主题的 onSurface 颜色
                action = {
                    TextButton(onClick = { showAddSuccessSnackbar = false }) {
                        Text(
                            "知道了",
                            color = MaterialTheme.colors.primary  // 使用主题的 primary 颜色
                        )
                    }
                }
            ) {
                Text(
                    "诗词已添加到系统",
                    color = MaterialTheme.colors.onSurface  // 使用主题的 onSurface 颜色
                )
            }
            
            LaunchedEffect(showAddSuccessSnackbar) {
                delay(2000)
                showAddSuccessSnackbar = false
            }
        }
    }
}

