import manager.AIModelManager
import service.AIService
import service.OpenAIService

object AIServiceFactory {
    fun createService(): AIService {
        val config = AIModelManager.getConfig()
        return OpenAIService(
            apiKey = config.apiKey,
            baseUrl = config.baseUrl,
            model = config.modelVersion
        )
    }
}
