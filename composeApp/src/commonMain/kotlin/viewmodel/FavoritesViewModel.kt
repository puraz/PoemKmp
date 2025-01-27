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
            repository.getFavoritePoems().collectLatest { poems ->
                _favoritePoems.value = poems
                // 更新选中诗词的状态
                _selectedPoem.value?.let { selected ->
                    _selectedPoem.value = poems.find { it.id == selected.id }
                }
            }
        }
    }

    fun onPoemSelected(poem: Poem_entity) {
        _selectedPoem.value = poem
    }

    fun toggleFavorite(poem: Poem_entity) {
        // 立即更新 UI 状态
        val updatedPoem = poem.copy(
            is_favorite = if (poem.is_favorite == 0L) 1L else 0L
        )
        
        // 更新选中的诗词
        _selectedPoem.value = updatedPoem
        
        // 更新列表
        _favoritePoems.value = if (updatedPoem.is_favorite == 0L) {
            // 如果取消收藏，从列表中移除
            _favoritePoems.value.filter { it.id != poem.id }
        } else {
            // 如果添加收藏，更新列表中的状态
            _favoritePoems.value.map { 
                if (it.id == poem.id) updatedPoem else it 
            }
        }
        
        // 异步更新数据库
        viewModelScope.launch {
            repository.toggleFavorite(
                id = poem.id,
                isFavorite = updatedPoem.is_favorite == 1L
            )
        }
    }
} 