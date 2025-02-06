package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ui.components.LoadingIndicator
import ui.components.PoemDetail
import ui.components.PoemListItem
import viewmodel.ViewModelFactory

@Composable
fun FavoritesScreen(
    viewModelFactory: ViewModelFactory,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colors // 获取当前主题颜色
    val viewModel = remember { viewModelFactory.createFavoritesViewModel() }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        when {
            viewModel.isLoading.value -> {
                // 使用自定义的加载指示器
                LoadingIndicator(
                    modifier = Modifier.fillMaxSize()
                )
            }
            viewModel.favoritePoems.value.isEmpty() -> {
                // 空状态显示
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "暂无收藏",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击诗词详情页的收藏按钮来添加收藏",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
            else -> {
                // 收藏列表和详情
                Row(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = 20.dp)
                ) {
                    // 收藏列表
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // 标题栏
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            // elevation = 1.dp
                        ) {
                            Text(
                                text = "收藏夹 (${viewModel.favoritePoems.value.size})",
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = viewModel.favoritePoems.value,
                                key = { it.id }
                            ) { poem ->
                                PoemListItem(
                                    poem = poem,
                                    onClick = { viewModel.onPoemSelected(poem) },
                                    showFavoriteIcon = true
                                )
                            }
                        }
                    }

                    // 诗词详情
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .padding(top = 25.dp)
                    ) {
                        viewModel.selectedPoem.value?.let { poem ->
                            PoemDetail(
                                poem = poem,
                                onFavoriteClick = { viewModel.toggleFavorite(poem) },
                                viewModelFactory = viewModelFactory
                            )
                        }
                    }
                }
            }
        }
    }
} 