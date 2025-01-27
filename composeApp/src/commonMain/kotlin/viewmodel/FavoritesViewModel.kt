package viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import data.PoemRepository
import data.db.Poem_entity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: PoemRepository
) : BaseViewModel() {
    private val _favoritePoems = mutableStateOf<List<Poem_entity>>(emptyList())
    val favoritePoems: State<List<Poem_entity>> = _favoritePoems.asState()

    private val _selectedPoem = mutableStateOf<Poem_entity?>(null)
    val selectedPoem: State<Poem_entity?> = _selectedPoem.asState()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            // TODO: 实现加载收藏诗词的逻辑
        }
    }

    fun onPoemSelected(poem: Poem_entity) {
        _selectedPoem.value = poem
    }
} 