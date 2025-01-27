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
import kotlinx.coroutines.delay
import service.AISearchResult
import viewmodel.AISearchViewModel

@Composable
fun AISearchPanel(
    viewModel: AISearchViewModel,
    onPoemSelected: (Poem_entity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var showAddSuccessSnackbar by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        label = { Text("搜索新诗词") },
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
                        }
                    }
                    viewModel.searchResults.value.isEmpty() -> {
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
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
        
        // 添加成功提示
        if (showAddSuccessSnackbar) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                action = {
                    TextButton(onClick = { showAddSuccessSnackbar = false }) {
                        Text("知道了")
                    }
                }
            ) {
                Text("诗词已添加到系统")
            }
            
            // 自动隐藏 Snackbar
            LaunchedEffect(showAddSuccessSnackbar) {
                delay(2000)
                showAddSuccessSnackbar = false
            }
        }
    }
}

