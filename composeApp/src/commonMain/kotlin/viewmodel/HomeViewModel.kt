package viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import data.PoemRepository
import data.db.Poem_entity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PoemRepository
) : BaseViewModel() {
    private val _poems = mutableStateOf<List<Poem_entity>>(emptyList())
    val poems: State<List<Poem_entity>> = _poems.asState()

    private val _selectedPoem = mutableStateOf<Poem_entity?>(null)
    val selectedPoem: State<Poem_entity?> = _selectedPoem.asState()

    private val _searchText = mutableStateOf("")
    val searchText: State<String> = _searchText.asState()

    init {
        loadPoems()
    }

    private fun loadPoems() {
        viewModelScope.launch {
            repository.getAllPoems().collectLatest { poemList ->
                _poems.value = poemList
            }
        }
    }

    fun onPoemSelected(poem: Poem_entity) {
        _selectedPoem.value = poem
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        // TODO: 实现搜索逻辑
    }

    fun toggleFavorite(poem: Poem_entity) {
        viewModelScope.launch {
            // TODO: 实现收藏切换逻辑
        }
    }
} 