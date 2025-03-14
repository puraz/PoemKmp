package service

import data.db.Poem_entity
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import model.DeepSeekModels

class DeepSeekAIService(
    private val apiKey: String,
    private val baseUrl: String,
    private val modelVersion: String
) : BaseAIService(), AIService {
    
    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val frequency_penalty: Int = 0,
        val max_tokens: Int = 4096,
        val presence_penalty: Int = 0,
        val response_format: ResponseFormat? = null,
        val temperature: Double = 0.3,
        val top_p: Double = 1.0,
        val stream: Boolean = false
    )

    @Serializable
    private data class Message(
        val role: String,
        val content: String
    )

    @Serializable
    private data class ChatResponse(
        val choices: List<Choice>
    ) {
        @Serializable
        data class Choice(
            val message: Message
        )
    }

    @Serializable
    private data class ResponseFormat(
        val type: String = "json_object"
    )

    @Serializable
    private data class PromptTokensDetails(
        val cached_tokens: Int
    )

    @Serializable
    private data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int,
        val prompt_tokens_details: PromptTokensDetails,
        val prompt_cache_hit_tokens: Int,
        val prompt_cache_miss_tokens: Int
    )

    @Serializable
    private data class Choice(
        val index: Int,
        val message: Message,
        val logprobs: String? = null,
        val finish_reason: String
    )

    @Serializable
    private data class SearchResult(
        val title: String,
        val author: String,
        val dynasty: String?,
        val content: String,
        val relevance_score: Double,
        val match_reason: String
    )

    @Serializable
    private data class SearchResponse(
        val recommendations: List<SearchResult>
    )

    @Serializable
    private data class ErrorResponse(
        val error: Error
    ) {
        @Serializable
        data class Error(
            val message: String,
            val type: String,
            val code: Int? = null
        )
    }

    override suspend fun semanticSearch(
        query: String,
        poems: List<Poem_entity>
    ): List<AISearchResult> {
        try {
            val systemPrompt = createSystemPrompt(query)
            val userPrompt = createUserPrompt(query, poems)

            val response = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(ChatRequest(
                    model = modelVersion,
                    messages = listOf(
                        Message("system", systemPrompt),
                        Message("user", userPrompt)
                    ),
                    response_format = if (modelVersion == "deepseek-reasoner") null else ResponseFormat()
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val chatResponse = response.body<ChatResponse>()
                    val content = chatResponse.choices.firstOrNull()?.message?.content
                        ?: throw AIServiceException.InvalidResponseError("响应内容为空")
                    
                    // 验证并可能修正 JSON 响应格式
                    val validatedContent = validateJsonResponse(content, expectArray = true)
                    
                    // 解析并验证结果
                    val results = json.decodeFromString<List<AISearchResult>>(validatedContent)
                    validateSearchResults(results)
                    return results
                }
                HttpStatusCode.Unauthorized -> 
                    throw AIServiceException.AuthenticationError("API Key 无效")
                HttpStatusCode.TooManyRequests -> 
                    throw AIServiceException.RateLimitError("超过请求限制")
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    throw AIServiceException.APIError(
                        response.status.value,
                        errorBody.error.message
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                is AIServiceException -> throw e
                else -> throw AIServiceException.NetworkError(e.message ?: "未知错误", e)
            }
        }
    }
    
    override suspend fun analyzePoemContent(poem: Poem_entity): PoemAnalysis = withRetry {
        try {
            val prompt = createAnalysisPrompt(poem)

            val response = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(ChatRequest(
                    model = modelVersion,
                    messages = listOf(Message("user", prompt)),
                    response_format = if (modelVersion == "deepseek-reasoner") null else ResponseFormat()
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val chatResponse = response.body<ChatResponse>()
                    val content = chatResponse.choices.firstOrNull()?.message?.content
                        ?: throw AIServiceException.InvalidResponseError("响应内容为空")
                    
                    // 验证 JSON 响应格式（对于 PoemAnalysis，我们期望是一个对象而不是数组）
                    validateJsonResponse(content, expectArray = false)
                    
                    val analysis = json.decodeFromString<PoemAnalysis>(content)
                    // validatePoemAnalysis(analysis)
                    analysis
                }
                HttpStatusCode.Unauthorized -> 
                    throw AIServiceException.AuthenticationError("API Key 无效")
                HttpStatusCode.TooManyRequests -> 
                    throw AIServiceException.RateLimitError("超过请求限制")
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    throw AIServiceException.APIError(
                        response.status.value,
                        errorBody.error.message
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                is AIServiceException -> throw e
                else -> throw AIServiceException.NetworkError(e.message ?: "未知错误", e)
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

    suspend fun getAvailableModels(): List<String> = withRetry {
        try {
            val response = client.get("$baseUrl/models") {
                header("Authorization", "Bearer $apiKey")
                accept(ContentType.Application.Json)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val models = response.body<DeepSeekModels>()
                    models.data.map { it.id }
                }

                HttpStatusCode.Unauthorized ->
                    throw AIServiceException.AuthenticationError("DeepSeek API Key 无效")

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
}

