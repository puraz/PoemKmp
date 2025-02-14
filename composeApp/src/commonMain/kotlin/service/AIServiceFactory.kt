import manager.AIModelManager
import service.AIService
import service.DeepSeekAIService
import service.GeminiAIService

object AIServiceFactory {
    fun createService(model: AIModelManager.AIModel): AIService {
        val config = AIModelManager.getModelConfig(model)

        return when (model) {
            AIModelManager.AIModel.DEEPSEEK -> DeepSeekAIService(
                apiKey = config.apiKey,
                baseUrl = config.baseUrl,
                modelVersion = config.modelVersion
            )

            AIModelManager.AIModel.GEMINI -> GeminiAIService(
                apiKey = config.apiKey,
                baseUrl = config.baseUrl,
                modelVersion = config.modelVersion
            )
        }
    }
} 