package service

import data.db.Poem_entity
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class DeepSeekAIService(
    private val apiKey: String,
    private val baseUrl: String = "https://api.deepseek.com/v1"
) : BaseAIService(), AIService {
    
    @Serializable
    private data class ChatRequest(
        val model: String = "deepseek-chat",
        val messages: List<Message>,
        val frequency_penalty: Int = 0,
        val max_tokens: Int = 4096,
        val presence_penalty: Int = 0,
        val response_format: ResponseFormat = ResponseFormat(),
        val temperature: Double = 0.7,
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
        val results: List<SearchResult>
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
    ): List<AISearchResult> = withRetry {
        try {
            val systemPrompt = createSystemPrompt(query)
            val userPrompt = if (poems.isEmpty()) {
                "请基于我的搜索意图推荐一些古诗词。要求返回 JSON 格式，包含 title, content, author, dynasty, relevanceScore, matchReason 字段。"
            } else {
                "请分析以下诗词是否符合我的搜索意图，并按相关度排序。诗词列表：\n" +
                poems.joinToString("\n") { "${it.title} - ${it.author}：${it.content}" }
            }

            val response = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(ChatRequest(
                    messages = listOf(
                        Message("system", systemPrompt),
                        Message("user", userPrompt)
                    )
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val chatResponse = response.body<ChatResponse>()
                    val content = chatResponse.choices.firstOrNull()?.message?.content
                        ?: throw AIServiceException.InvalidResponseError("响应内容为空")
                    
                    // 验证 JSON 响应格式
                    validateJsonResponse(content)
                    
                    // 解析并验证结果
                    val results = json.decodeFromString<List<AISearchResult>>(content)
                    validateSearchResults(results)
                    results
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
            val prompt = """
                请分析这首诗：
                《${poem.title}》 - ${poem.author}
                ${poem.content}
                
                请从以下维度进行分析，并以 JSON 格式返回：
                1. theme: 主题思想
                2. style: 写作风格
                3. interpretation: 诗歌赏析
                4. culturalContext: 文化背景
                5. literaryDevices: 写作手法 (数组)
                6. emotions: 情感特征 (数组)
            """.trimIndent()

            val response = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(ChatRequest(
                    messages = listOf(Message("user", prompt))
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val chatResponse = response.body<ChatResponse>()
                    val content = chatResponse.choices.firstOrNull()?.message?.content
                        ?: throw AIServiceException.InvalidResponseError("响应内容为空")
                    
                    validateJsonResponse(content)
                    
                    val analysis = json.decodeFromString<PoemAnalysis>(content)
                    validatePoemAnalysis(analysis)
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

    private fun validatePoemAnalysis(analysis: PoemAnalysis) {
        require(analysis.theme.isNotBlank()) { "主题思想不能为空" }
        require(analysis.style.isNotBlank()) { "写作风格不能为空" }
        require(analysis.interpretation.isNotBlank()) { "诗歌赏析不能为空" }
        require(analysis.culturalContext.isNotBlank()) { "文化背景不能为空" }
        require(analysis.literaryDevices.isNotEmpty()) { "写作手法不能为空" }
        require(analysis.emotions.isNotEmpty()) { "情感特征不能为空" }
    }
}

