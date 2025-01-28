package service

import data.db.Poem_entity
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeepSeekAIService(
    private val apiKey: String,
    private val baseUrl: String = "https://api.deepseek.com"
) : AIService {
    
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
    }
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    @Serializable
    private data class Message(
        val role: String,
        val content: String
    )

    @Serializable
    private data class ResponseFormat(
        val type: String = "json_object"
    )

    @Serializable
    private data class ChatRequest(
        val model: String = "deepseek-chat",
        val messages: List<Message>,
        val frequency_penalty: Int = 0,
        val max_tokens: Int = 2048,
        val presence_penalty: Int = 0,
        val response_format: ResponseFormat = ResponseFormat(),
        val temperature: Double = 0.7,
        val top_p: Double = 1.0,
        val stream: Boolean = false
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
    private data class ChatResponse(
        val id: String,
        val `object`: String = "chat.completion",
        val created: Long,
        val model: String,
        val choices: List<Choice>,
        val usage: Usage,
        val system_fingerprint: String
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
    
    override suspend fun semanticSearch(
        query: String,
        poems: List<Poem_entity>
    ): List<AISearchResult> {
        val systemPrompt = """
        作为一个专业的诗词搜索助手，你有两个主要任务：
        1. 如果提供了诗词列表，从中找出与查询意图最相关的结果。
        2. 如果没有提供诗词列表或列表为空，请根据查询意图推荐合适的诗词（从你的知识库中）。

        请以JSON格式返回结果，包含以下字段：
        - title: 诗词标题
        - author: 作者
        - dynasty: 朝代
        - content: 内容
        - relevance_score: 相关度评分 (0-1)
        - match_reason: 匹配原因或推荐理由
        - is_recommendation: 是否为推荐诗词（true/false）

        请将结果包装在 results 数组中。
        """.trimIndent()

        val userPrompt = if (poems.isEmpty()) {
            """
            系统中暂无诗词，请根据以下查询意图推荐合适的诗词：
            查询意图：$query
            """.trimIndent()
        } else {
            """
            查询意图：$query
            
            诗词列表：
            ${poems.joinToString("\n\n") { 
                """
                标题：${it.title}
                作者：${it.author}
                朝代：${it.dynasty ?: "未知"}
                内容：${it.content}
                """.trimIndent()
            }}
            """.trimIndent()
        }

        @Serializable
        data class SearchResult(
            val title: String,
            val author: String,
            val dynasty: String?,
            val content: String,
            val relevance_score: Double,
            val match_reason: String,
            val is_recommendation: Boolean = false
        )

        @Serializable
        data class SearchResponse(
            val results: List<SearchResult>
        )

        try {
            val request = ChatRequest(
                messages = listOf(
                    Message("system", systemPrompt),
                    Message("user", userPrompt)
                )
            )
            
            println("发送请求到 DeepSeek API...")
            println("请求内容: ${json.encodeToString(ChatRequest.serializer(), request)}")
            
            val response = client.post("$baseUrl/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(request)
            }
            
            println("收到响应，状态码: ${response.status}")
            
            if (!response.status.isSuccess()) {
                val errorBody = response.body<String>()
                println("错误响应内容: $errorBody")
                throw AIServiceException("API 请求失败: $errorBody")
            }

            val responseText = response.body<String>()
            println("原始响应内容: $responseText")
            
            val chatResponse = json.decodeFromString<ChatResponse>(responseText)
            val responseContent = chatResponse.choices.firstOrNull()?.message?.content
            println("AI 响应内容: $responseContent")
            
            if (responseContent == null) {
                throw AIServiceException("AI 响应内容为空")
            }
            
            val searchResponse = json.decodeFromString<SearchResponse>(responseContent)
            println("找到 ${searchResponse.results.size} 个${if (poems.isEmpty()) "推荐" else "匹配"}结果")
            
            return searchResponse.results.map { result ->
                AISearchResult(
                    title = result.title,
                    content = result.content,
                    author = result.author,
                    dynasty = result.dynasty,
                    category = null,
                    notes = null,
                    relevanceScore = result.relevance_score,
                    matchReason = result.match_reason,
                    isRecommendation = result.is_recommendation
                )
            }
        } catch (e: Exception) {
            println("AI 搜索出错: ${e.message}")
            e.printStackTrace()
            return emptyList()
            // throw AIServiceException("AI 搜索失败: ${e.message}")
        }
    }
    
    override suspend fun analyzePoemContent(poem: Poem_entity): PoemAnalysis {
        // 实现诗词赏析功能
        TODO("Not yet implemented")
    }

    private fun handleError(error: Throwable): Nothing {
        throw AIServiceException("AI 服务调用失败: ${error.message}")
    }
}

class AIServiceException(message: String) : Exception(message) 