package service

import data.db.Poem_entity
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class OpenAIService(
    private val apiKey: String,
    private val model: String = "gpt-4-turbo-preview",
    private val baseUrl: String = "https://api.openai.com/v1"
) : BaseAIService(), AIService {

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7
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
            val message: Message,
            val finish_reason: String
        )
    }

    @Serializable
    private data class ErrorResponse(
        val error: Error
    ) {
        @Serializable
        data class Error(
            val message: String,
            val type: String,
            val code: String? = null,
            val param: String? = null
        )
    }

    override suspend fun semanticSearch(
        query: String,
        poems: List<Poem_entity>
    ): List<AISearchResult> = withRetry {
        try {
            val systemPrompt = createSystemPrompt(query)
            val userPrompt = if (poems.isEmpty()) {
                "基于搜索意图推荐古诗词，返回 JSON 数组格式，包含 title, content, author, dynasty, relevanceScore, matchReason 字段。"
            } else {
                "分析并排序以下诗词：\n" +
                poems.joinToString("\n") { "${it.title} - ${it.author}：${it.content}" }
            }

            val response = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(ChatRequest(
                    model = model,
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
                    
                    if (chatResponse.choices.firstOrNull()?.finish_reason != "stop") {
                        throw AIServiceException.InvalidResponseError("响应未完全生成")
                    }
                    
                    validateJsonResponse(content)
                    val results = json.decodeFromString<List<AISearchResult>>(content)
                    validateSearchResults(results)
                    results
                }
                HttpStatusCode.Unauthorized -> 
                    throw AIServiceException.AuthenticationError("OpenAI API Key 无效")
                HttpStatusCode.TooManyRequests -> 
                    throw AIServiceException.RateLimitError("OpenAI API 请求频率超限")
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    throw AIServiceException.APIError(
                        response.status.value,
                        "OpenAI API 错误: ${errorBody.error.message}"
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                is AIServiceException -> throw e
                else -> throw AIServiceException.NetworkError("OpenAI API 调用失败: ${e.message}", e)
            }
        }
    }

    override suspend fun analyzePoemContent(poem: Poem_entity): PoemAnalysis = withRetry {
        try {
            val prompt = """
                分析这首诗：
                《${poem.title}》 - ${poem.author}
                ${poem.content}
                
                请以 JSON 格式返回分析结果，必须包含以下字段：
                - theme: 主题思想
                - style: 写作风格
                - interpretation: 诗歌赏析
                - culturalContext: 文化背景
                - literaryDevices: 写作手法数组
                - emotions: 情感特征数组
            """.trimIndent()

            val response = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(ChatRequest(
                    model = model,
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
                    // validatePoemAnalysis(analysis)
                    analysis
                }
                HttpStatusCode.Unauthorized -> 
                    throw AIServiceException.AuthenticationError("OpenAI API Key 无效")
                HttpStatusCode.TooManyRequests -> 
                    throw AIServiceException.RateLimitError("OpenAI API 请求频率超限")
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    throw AIServiceException.APIError(
                        response.status.value,
                        "OpenAI API 错误: ${errorBody.error.message}"
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                is AIServiceException -> throw e
                else -> throw AIServiceException.NetworkError("OpenAI API 调用失败: ${e.message}", e)
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

    // private fun validatePoemAnalysis(analysis: PoemAnalysis) {
    //     require(analysis.theme.isNotBlank()) { "主题思想不能为空" }
    //     require(analysis.style.isNotBlank()) { "写作风格不能为空" }
    //     require(analysis.interpretation.isNotBlank()) { "诗歌赏析不能为空" }
    //     require(analysis.culturalContext.isNotBlank()) { "文化背景不能为空" }
    //     require(analysis.literaryDevices.isNotEmpty()) { "写作手法不能为空" }
    //     require(analysis.emotions.isNotEmpty()) { "情感特征不能为空" }
    // }
} 