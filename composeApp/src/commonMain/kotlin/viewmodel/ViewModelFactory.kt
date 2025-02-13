package viewmodel

import data.PoemRepository
import manager.AIModelManager
import service.AIService
import service.DeepSeekAIService
import service.GeminiAIService

class ViewModelFactory(
    private val repository: PoemRepository
) {
    fun createHomeViewModel(): HomeViewModel = HomeViewModel(repository)
    fun createFavoritesViewModel(): FavoritesViewModel = FavoritesViewModel(repository)
    fun createSearchViewModel(): SearchViewModel = SearchViewModel(repository)
    fun createPoemAppreciationViewModel(): PoemAppreciationViewModel = PoemAppreciationViewModel(
        aiService = createAIService(),
        poemRepository = repository
    )
    fun createAISearchViewModel(): AISearchViewModel = AISearchViewModel(
        poemRepository = repository,
        aiService = createAIService()
    )

    // 后备方法，以防 currentAIService 为空
    private fun createAIService(): AIService {
        return when (AIModelManager.currentModel.value) {
            AIModelManager.AIModel.DEEPSEEK -> DeepSeekAIService(
                apiKey = AIModelManager.getApiKey()
            )

            // AIModelManager.AIModel.OPENAI -> OpenAIService(
            //     apiKey = AIModelManager.getApiKey()
            // )
            //
            AIModelManager.AIModel.GEMINI -> GeminiAIService(
                apiKey = AIModelManager.getApiKey()
            )
        }
    }
}