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
            val systemPrompt = """
                你是一个专业的古诗词检索助手。用户可能会提供：
                1. 诗词的片段或关键词
                2. 诗词的主题或意境描述
                3. 对诗词内容的模糊记忆
                
                你的任务是：
                1. 理解用户的搜索意图
                2. 找到最匹配的完整诗词
                3. 确保返回的诗词内容完整准确，并按照以下格式规范：
                   - 标题单独一行
                   - 正文从标题下一行开始
                   - 每句诗词单独成行
                   - 只在需要分段的诗词中添加空行（如乐府诗、长诗等）
                   - 一般的五言、七言绝句和律诗无需分段
                   例如：
                   《登高》
                   风急天高猿啸哀，
                   渚清沙白鸟飞回。
                   无边落木萧萧下，
                   不尽长江滚滚来。
                   
                   分段示例（乐府诗）：
                   《长恨歌》
                   汉皇重色思倾国，
                   御宇多年求不得。
                   
                   杨家有女初长成，
                   养在深闺人未识。
                4. 对每首诗词解释为什么它匹配用户的搜索
                
                相关度评分标准(relevanceScore)：
                1.0: 完全匹配
                - 用户输入的内容与诗词完全吻合
                - 例如：用户输入"床前明月光"，找到《静夜思》
                
                0.8-0.9: 高度相关
                - 包含用户输入的关键词或短语
                - 主题和意境高度吻合
                - 例如：用户描述"思乡之情"，找到《乡愁》
                
                0.6-0.7: 中度相关
                - 主题相似但表达方式不同
                - 部分关键词匹配
                - 例如：用户搜索"离别"主题，找到描写送别的诗
                
                0.4-0.5: 一般相关
                - 意境或主题有部分重叠
                - 包含相关的意象或典故
                - 例如：用户搜索"春天"，找到描写其他季节但有相似意境的诗
                
                0.1-0.3: 低度相关
                - 仅有少量关联元素
                - 主题相近但不完全匹配
                
                0.0: 完全不相关
                - 与搜索意图没有明显关联
                
                请注意：
                - 优先返回与用户输入最相关的诗词
                - 确保诗词的完整性，包括标题、作者、朝代和全文
                - 根据诗词类型正确处理换行和分段
                - 解释匹配原因时要具体说明与用户查询的关联
                - 严格按照相关度评分标准进行打分
            """.trimIndent()
            
            val userPrompt = if (poems.isEmpty()) {
                """
                基于我的搜索意图"$query"，请推荐最匹配的古诗词。要求：
                1. 返回完整的诗词内容，并确保正确的换行和分段格式
                2. 包含标题、作者、朝代、全文
                3. 说明为什么这首诗词匹配我的搜索
                4. 返回 JSON 数组格式，数组直接在最外层，不需要有名称
                5. 数组中的对象包含 title, content, author, dynasty, relevanceScore, matchReason 字段
                6. content 字段中的换行使用 \n 表示，段落之间使用 \n\n 表示
                """.trimIndent()
            } else {
                """
                请分析以下诗词是否符合我的搜索意图"$query"，并按相关度排序。
                诗词列表：
                ${poems.joinToString("\n") { "${it.title} - ${it.author}：${it.content}" }}
                """.trimIndent()
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
                    println("chatResponse: $chatResponse")
                    val content = chatResponse.choices.firstOrNull()?.message?.content
                        ?: throw AIServiceException.InvalidResponseError("响应内容为空")
                    
                    // 验证 JSON 响应格式
                    validateJsonResponse(content)
                    
                    // 解析并验证结果
                    val results = json.decodeFromString<List<AISearchResult>>(content)
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

