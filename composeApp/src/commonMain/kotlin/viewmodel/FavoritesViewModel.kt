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
    val favoritePoems: State<List<Poem_entity>> = _favoritePoems

    private val _selectedPoem = mutableStateOf<Poem_entity?>(null)
    val selectedPoem: State<Poem_entity?> = _selectedPoem

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getFavoritePoems().collectLatest { poems ->
                    _favoritePoems.value = poems
                    _selectedPoem.value = _selectedPoem.value?.let { selected ->
                        poems.find { it.id == selected.id }
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                println("加载收藏失败: ${e.message}")
            }
        }
    }

    fun onPoemSelected(poem: Poem_entity) {
        _selectedPoem.value = poem
    }

    fun toggleFavorite(poem: Poem_entity) {
        viewModelScope.launch {
            try {
                val updatedPoem = poem.copy(
                    is_favorite = if (poem.is_favorite == 0L) 1L else 0L
                )

                if (updatedPoem.is_favorite == 0L) {
                    _selectedPoem.value = null
                } else {
                    _selectedPoem.value = updatedPoem
                }

                _favoritePoems.value = if (updatedPoem.is_favorite == 0L) {
                    _favoritePoems.value.filter { it.id != poem.id }
                } else {
                    _favoritePoems.value.map {
                        if (it.id == poem.id) updatedPoem else it
                    }
                }

                repository.toggleFavorite(
                    id = poem.id,
                    isFavorite = updatedPoem.is_favorite == 1L
                )
            } catch (e: Exception) {
                println("更新收藏状态失败: ${e.message}")
            }
        }
    }
} 