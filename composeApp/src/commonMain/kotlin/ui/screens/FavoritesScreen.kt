package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ui.components.PoemDetail
import ui.components.PoemListItem
import viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.favoritePoems.value.isEmpty()) {
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
        } else {
            // 收藏列表和详情
            Row(modifier = Modifier.fillMaxSize()) {
                // 收藏列表
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                ) {
                    // 标题栏
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 1.dp
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
                ) {
                    viewModel.selectedPoem.value?.let { poem ->
                        PoemDetail(
                            poem = poem,
                            onFavoriteClick = { viewModel.toggleFavorite(poem) }
                        )
                    }
                }
            }
        }
    }
} 