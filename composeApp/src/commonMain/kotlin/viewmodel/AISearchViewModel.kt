package viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import data.PoemRepository
import data.db.Poem_entity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import service.AISearchResult
import service.AIService

class AISearchViewModel(
    private val poemRepository: PoemRepository,
    private val aiService: AIService
) : BaseViewModel() {
    private val _searchResults = mutableStateOf<List<AISearchResult>>(emptyList())
    val searchResults: State<List<AISearchResult>> = _searchResults.asState()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading.asState()

    // 语义搜索
    fun semanticSearch(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. 获取系统中所有诗词
                val allPoems = poemRepository.getAllPoems().first()
                
                // 2. 使用 AI 进行语义搜索
                val aiResults = aiService.semanticSearch(
                    query = query,
                    poems = allPoems
                )
                
                // 3. 转换结果并排序
                _searchResults.value = aiResults.sortedByDescending { it.relevanceScore }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 将 AI 搜索结果转换为系统诗词
    fun convertToSystemPoem(aiResult: AISearchResult) {
        viewModelScope.launch {
            try {
                val poem = Poem_entity(
                    id = 0, // 自动生成
                    title = aiResult.title,
                    content = aiResult.content,
                    author = aiResult.author,
                    dynasty = aiResult.dynasty,
                    category = aiResult.category ?: "其他",
                    notes = aiResult.notes,
                    create_time = System.currentTimeMillis(),
                    update_time = System.currentTimeMillis(),
                    is_favorite = 0
                )
                poemRepository.addPoem(
                    title = poem.title,
                    content = poem.content,
                    author = poem.author,
                    dynasty = poem.dynasty,
                    category = poem.category,
                    notes = poem.notes
                )
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
} 