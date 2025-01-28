package viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import data.PoemRepository
import data.db.Poem_entity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(
    private val poemRepository: PoemRepository
) : BaseViewModel() {
    private val _searchResults = mutableStateOf<List<Poem_entity>>(emptyList())
    val searchResults: State<List<Poem_entity>> = _searchResults.asState()

    private val _isLoading = mutableStateOf(false)

    fun search(keyword: String) {
        if (keyword.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val poemList = poemRepository.getAllPoems().first()
                val results = poemList.filter { poem ->
                    poem.title.contains(keyword) || 
                    poem.author.contains(keyword) ||
                    poem.content.contains(keyword)
                }
                _searchResults.value = results
            } finally {
                _isLoading.value = false
            }
        }
    }
} 