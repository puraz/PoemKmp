package viewmodel

import AIServiceFactory
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import data.PoemRepository
import data.db.Poem_entity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import manager.AIModelManager
import service.AISearchResult
import service.AIService

class AISearchViewModel(
    private val poemRepository: PoemRepository,
    private var aiService: AIService
) : BaseViewModel() {
    init {
        // 监听 AI 模型变化
        AIModelManager.addModelChangeListener {
            val currentModel = AIModelManager.currentModel.value
            val aiService = AIServiceFactory.createService(currentModel)
            this.aiService = aiService
        }
    }

    private val _searchResults = mutableStateOf<List<AISearchResult>>(emptyList())
    val searchResults: State<List<AISearchResult>> = _searchResults.asState()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading.asState()

    // 系统内语义搜索
    fun searchInSystem(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allPoems = poemRepository.getAllPoems().first()
                val aiResults = aiService.semanticSearch(
                    query = query,
                    poems = allPoems
                )
                _searchResults.value = aiResults.sortedByDescending { it.relevanceScore }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 系统外搜索推荐
    fun searchOutsideSystem(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("ai service : $aiService")
                val aiResults = aiService.semanticSearch(
                    query = query,
                    poems = emptyList()  // 传入空列表表示搜索系统外的诗词
                )
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
                    notes = null,
                    create_time = System.currentTimeMillis(),
                    update_time = System.currentTimeMillis(),
                    is_favorite = 0,
                    appreciation_content = null
                )
                poemRepository.addPoem(
                    title = poem.title,
                    content = poem.content,
                    author = poem.author,
                    dynasty = poem.dynasty,
                    category = poem.category,
                    notes = poem.notes
                )
                
                // 刷新搜索结果，移除已添加的诗词
                _searchResults.value = _searchResults.value.filter { it != aiResult }
                
            } catch (e: Exception) {
                // TODO: 处理错误，可以添加错误提示
                println("添加诗词失败: ${e.message}")
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    // 在 ViewModel 销毁时移除监听器
    override fun onCleared() {
        AIModelManager.removeModelChangeListener { /* 对应的监听器逻辑 */ }
    }
} 