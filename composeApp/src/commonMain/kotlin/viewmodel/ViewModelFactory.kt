package viewmodel

import data.PoemRepository
import service.AIService
import service.DeepSeekAIService

class ViewModelFactory(
    private val repository: PoemRepository,
    private val apiKey: String,  // DeepSeek API Key
    private val baseUrl: String = "https://api.deepseek.com"
) {
    // 懒加载创建 AIService 实例
    private val aiService: AIService by lazy {
        DeepSeekAIService(
            apiKey = apiKey,
            baseUrl = baseUrl
        )
    }

    fun createHomeViewModel(): HomeViewModel = HomeViewModel(repository)
    
    fun createFavoritesViewModel(): FavoritesViewModel = FavoritesViewModel(repository)
    fun createSearchViewModel(): SearchViewModel = SearchViewModel(repository)

    fun createAISearchViewModel(): AISearchViewModel = AISearchViewModel(
        poemRepository = repository,
        aiService = aiService
    )
}