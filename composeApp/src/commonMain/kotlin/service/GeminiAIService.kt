package service

import data.db.Poem_entity
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import model.GeminiModels

class GeminiAIService(
    private val apiKey: String,
    private val baseUrl: String,
    private val modelVersion: String
) : BaseAIService(), AIService {

    @Serializable
    private data class GeminiRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig = GenerationConfig(),
    ) {
        @Serializable
        data class Content(
            val role: String,
            val parts: List<Part>
        )
        
        @Serializable
        data class Part(
            val text: String
        )
        
        @Serializable
        data class GenerationConfig(
            val temperature: Double = 0.3,
            val response_mime_type: String = "application/json"
        )
    }

    @Serializable
    private data class GeminiResponse(
        val candidates: List<Candidate>,
        val promptFeedback: PromptFeedback? = null
    ) {
        @Serializable
        data class Candidate(
            val content: GeminiRequest.Content,
            val finishReason: String
        )
        
        @Serializable
        data class PromptFeedback(
            val blockReason: String? = null,
            val safetyRatings: List<SafetyRating>? = null
        )
        
        @Serializable
        data class SafetyRating(
            val category: String,
            val probability: String
        )
    }

    @Serializable
    private data class ErrorResponse(
        val error: Error
    ) {
        @Serializable
        data class Error(
            val code: Int,
            val message: String,
            val status: String
        )
    }

    override suspend fun semanticSearch(
        query: String,
        poems: List<Poem_entity>
    ): List<AISearchResult> = withRetry {
        try {
            val systemPrompt = createSystemPrompt(query)
            val userPrompt = createUserPrompt(query, poems)

            val response = client.post("$baseUrl/models/$modelVersion:generateContent?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(
                    contents = listOf(
                        GeminiRequest.Content("user", listOf(
                            GeminiRequest.Part(systemPrompt),
                            GeminiRequest.Part(userPrompt)
                        ))
                    )
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val geminiResponse = response.body<GeminiResponse>()
                    
                    geminiResponse.promptFeedback?.blockReason?.let { reason ->
                        throw AIServiceException.InvalidResponseError("内容被拒绝: $reason")
                    }
                    
                    val content = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: throw AIServiceException.InvalidResponseError("响应内容为空")
                    
                    if (geminiResponse.candidates.firstOrNull()?.finishReason != "STOP") {
                        throw AIServiceException.InvalidResponseError("响应未完全生成")
                    }
                    
                    validateJsonResponse(content)
                    val results = json.decodeFromString<List<AISearchResult>>(content)
                    validateSearchResults(results)
                    results
                }
                HttpStatusCode.Unauthorized -> 
                    throw AIServiceException.AuthenticationError("Gemini API Key 无效")
                HttpStatusCode.TooManyRequests -> 
                    throw AIServiceException.RateLimitError("Gemini API 请求频率超限")
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    throw AIServiceException.APIError(
                        response.status.value,
                        "Gemini API 错误: ${errorBody.error.message}"
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                is AIServiceException -> throw e
                else -> throw AIServiceException.NetworkError("Gemini API 调用失败: ${e.message}", e)
            }
        }
    }

    override suspend fun analyzePoemContent(poem: Poem_entity): PoemAnalysis = withRetry {
        try {
            val prompt = createAnalysisPrompt(poem)

            val response = client.post("$baseUrl/models/$modelVersion:generateContent?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(
                    contents = listOf(
                        GeminiRequest.Content("user", listOf(GeminiRequest.Part(prompt)))
                    ),
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val geminiResponse = response.body<GeminiResponse>()
                    
                    geminiResponse.promptFeedback?.blockReason?.let { reason ->
                        throw AIServiceException.InvalidResponseError("内容被拒绝: $reason")
                    }
                    
                    val content = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: throw AIServiceException.InvalidResponseError("响应内容为空")
                    
                    validateJsonResponse(content)
                    val analysis = json.decodeFromString<PoemAnalysis>(content)
                    // validatePoemAnalysis(analysis)
                    analysis
                }
                HttpStatusCode.Unauthorized -> 
                    throw AIServiceException.AuthenticationError("Gemini API Key 无效")
                HttpStatusCode.TooManyRequests -> 
                    throw AIServiceException.RateLimitError("Gemini API 请求频率超限")
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    throw AIServiceException.APIError(
                        response.status.value,
                        "Gemini API 错误: ${errorBody.error.message}"
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                is AIServiceException -> throw e
                else -> throw AIServiceException.NetworkError("Gemini API 调用失败: ${e.message}", e)
            }
        }
    }

    suspend fun getAvailableModels(): List<GeminiModels.ModelInfo> = withRetry {
        try {
            val response = client.get("$baseUrl/models") {
                parameter("key", apiKey)
                accept(ContentType.Application.Json)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val models = response.body<GeminiModels>()
                    models.models
                }

                HttpStatusCode.Unauthorized ->
                    throw AIServiceException.AuthenticationError("Gemini API Key 无效")

                else -> throw AIServiceException.APIError(
                    response.status.value,
                    "获取模型列表失败"
                )
            }
        } catch (e: Exception) {
            when (e) {
                is AIServiceException -> throw e
                else -> throw AIServiceException.NetworkError("获取模型列表失败: ${e.message}", e)
            }
        }
    }

    private fun validateSearchResults(results: List<AISearchResult>) {
        if (results.isEmpty()) {
            throw AIServiceException.InvalidResponseError("搜索结果为空")
        }
        
        results.forEach { result ->
            require(result.title.isNotBlank()) { "标题不能为空" }
            require(result.content.isNotBlank()) { "内容不能为空" }
            require(result.author.isNotBlank()) { "作者不能为空" }
            require(result.relevanceScore in 0.0..1.0) { "相关度分数必须在 0-1 之间" }
            require(result.matchReason.isNotBlank()) { "匹配原因不能为空" }
        }
    }

    /*private fun validatePoemAnalysis(analysis: PoemAnalysis) {
        require(analysis.theme.isNotBlank()) { "主题思想不能为空" }
        require(analysis.style.isNotBlank()) { "写作风格不能为空" }
        require(analysis.interpretation.isNotBlank()) { "诗歌赏析不能为空" }
        require(analysis.culturalContext.isNotBlank()) { "文化背景不能为空" }
        require(analysis.literaryDevices.isNotEmpty()) { "写作手法不能为空" }
        require(analysis.emotions.isNotEmpty()) { "情感特征不能为空" }
    }*/
}