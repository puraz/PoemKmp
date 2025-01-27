package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import ui.components.PoemDetail
import ui.components.PoemListItem
import viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        // 诗词列表
        Column(
            modifier = Modifier.weight(0.4f)
                .fillMaxHeight()
                .padding(top = 16.dp)  // 添加顶部间距
        ) {
            LazyColumn {
                items(viewModel.poems.value) { poem ->
                    PoemListItem(
                        poem = poem,
                        onClick = { viewModel.onPoemSelected(poem) }
                    )
                }
            }
        }
        
        // 诗词详情
        Box(
            modifier = Modifier.weight(0.6f)
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

@Composable
fun <T> Flow<T>.collectAsState(
    initial: T
): State<T> {
    val state = remember { mutableStateOf(initial) }
    LaunchedEffect(Unit) {
        collect { state.value = it }
    }
    return state
} 