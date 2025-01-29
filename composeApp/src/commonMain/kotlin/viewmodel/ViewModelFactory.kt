package viewmodel

import data.PoemRepository
import manager.AIModelManager
import service.AIService
import service.DeepSeekAIService
import service.GeminiAIService
import service.OpenAIService

class ViewModelFactory(
    private val repository: PoemRepository
) {
    private fun createAIService(): AIService {
        return when (AIModelManager.currentModel.value) {
            AIModelManager.AIModel.DEEPSEEK -> DeepSeekAIService(
                apiKey = AIModelManager.getApiKey()
            )
            AIModelManager.AIModel.OPENAI -> OpenAIService(
                apiKey = AIModelManager.getApiKey()
            )
            AIModelManager.AIModel.GEMINI -> GeminiAIService(
                apiKey = AIModelManager.getApiKey()
            )
        }
    }

    fun createHomeViewModel(): HomeViewModel = HomeViewModel(repository)
    fun createFavoritesViewModel(): FavoritesViewModel = FavoritesViewModel(repository)
    fun createSearchViewModel(): SearchViewModel = SearchViewModel(repository)
    fun createAISearchViewModel(): AISearchViewModel = AISearchViewModel(
        poemRepository = repository,
        aiService = createAIService()
    )
}