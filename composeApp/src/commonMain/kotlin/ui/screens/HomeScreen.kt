package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.PoemRepository
import data.db.Poem_entity
import kotlinx.coroutines.flow.Flow
import ui.components.PoemDetail
import ui.components.PoemListItem
import ui.components.SearchBar

@Composable
fun HomeScreen(repository: PoemRepository) {
    var searchText by remember { mutableStateOf("") }
    var selectedPoem by remember { mutableStateOf<Poem_entity?>(null) }
    
    val poems = repository.getAllPoems().collectAsState(initial = emptyList())
    
    Row(modifier = Modifier.fillMaxSize()) {
        // 诗词列表
        Column(
            modifier = Modifier.weight(0.4f)
                .fillMaxHeight()
        ) {
            // SearchBar(
            //     value = searchText,
            //     onValueChange = { searchText = it },
            //     modifier = Modifier.padding(vertical = 16.dp)
            // )
            LazyColumn {
                items(poems.value) { poem ->
                    PoemListItem(
                        poem = poem,
                        onClick = { selectedPoem = poem }
                    )
                }
            }
        }
        
        // 诗词详情
        Box(
            modifier = Modifier.weight(0.6f)
                .fillMaxHeight()
        ) {
            selectedPoem?.let { poem ->
                PoemDetail(
                    poem = poem,
                    onFavoriteClick = {
                        // TODO: 实现收藏功能
                    }
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