package service

import data.db.Poem_entity
import io.ktor.client.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

abstract class BaseAIService {
    protected val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
    }
    
    protected val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 60000
            socketTimeoutMillis = 60000
        }
        
        // 全局错误处理
        install(HttpCallValidator) {
            handleResponseExceptionWithRequest { cause, request ->
                throw when (cause) {
                    is HttpRequestTimeoutException ->
                        AIServiceException.NetworkError("请求超时: ${request.url}", cause)

                    is ConnectTimeoutException ->
                        AIServiceException.NetworkError("连接超时: ${request.url}", cause)

                    else -> AIServiceException.NetworkError(
                        "访问 ${request.url} 时发生错误: ${cause.message ?: "未知网络错误"}",
                        cause
                    )
                }
            }
        }
    }
    
    protected suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 60000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: AIServiceException) {
                if (e is AIServiceException.RateLimitError || 
                    e is AIServiceException.NetworkError) {
                    if (attempt == maxAttempts - 1) throw e
                    
                    delay(currentDelay)
                    currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
                } else throw e
            }
        }
        throw AIServiceException.NetworkError("超过最大重试次数")
    }
    
    protected fun validateJsonResponse(jsonString: String) {
        try {
            val jsonElement = json.parseToJsonElement(jsonString)
            if (jsonElement !is JsonArray && jsonElement !is JsonObject) {
                throw AIServiceException.ResponseParsingError("响应格式不正确")
            }
        } catch (e: Exception) {
            throw AIServiceException.ResponseParsingError("JSON解析失败", e)
        }
    }
    
    protected fun createSystemPrompt(query: String) = """
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

    protected fun createUserPrompt(query: String, poems: List<Poem_entity>) = if (poems.isEmpty()) {
        """
                基于我的搜索意图"$query"，请推荐最匹配的古诗词。要求：
                1. 返回完整的诗词内容，诗词的标题、作者、朝代、全文都要正确，并确保正确的换行和分段格式，
                2. 包含标题、作者、朝代、全文，朝代返回空字符串表示未知，标题不要带书名号“《”， 朝代比如“宋代”只需返回“宋”，**诗句要完整，不要只返回一两句**
                3. 说明为什么这首诗词匹配我的搜索
                4. 数组中的对象包含 title, content, author, dynasty, relevanceScore, matchReason 字段
                5. content 字段中的换行使用 \n 表示，段落之间使用 \n\n 表示
                6. 不能以```json开头，直接返回 JSON 数组格式
                7. **直接返回 JSON 数组格式，不需要使用 ```json 代码块，也不需要任何额外的说明或解释**，返回 JSON 数组格式，数组直接在最外层，不需要有名称
                8. 返回结果的 JSON 格式应为 List<AISearchResult>，其中 AISearchResult 对象的字段定义如下：
                AISearchResult = {
                    "title": 诗歌标题 (String),
                    "content": 诗歌内容 (String),
                    "author": 诗歌作者 (String),
                    "dynasty": 诗歌朝代 (String, 可以为空),
                    "category": 诗歌类别 (String, 可以为空),
                    "notes": 诗歌注释 (String, 可以为空),
                    "relevanceScore": 相关度评分 (Double, 范围在 0.0 到 1.0 之间),
                    "matchReason": 匹配原因 (String),
                    "isRecommendation": 是否推荐 (Boolean, 默认为 false)
                }
                请确保返回的 JSON 格式正确，可以直接被程序解析。
                """.trimIndent()
    } else {
        """
                请分析以下诗词是否符合我的搜索意图"$query"，并按相关度排序。 
                **注意：直接返回 JSON 数组，不需要 ```json 代码块**  
                诗词列表：
                ${poems.joinToString("\n") { "${it.title} - ${it.author}：${it.content}" }}
                """.trimIndent()
    }

    // protected fun createAnalysisPrompt(poem: Poem_entity) = """
    //             请分析这首诗：
    //             《${poem.title}》 - ${poem.author}
    //             ${poem.content}
    //
    //             **注意：直接返回 JSON ，不需要 ```json 代码块**，返回 JSON 格式应为 PoemAnalysis 对象，而不是 PoemAnalysis 对象数组，字段定义如下：
    //             请从以下维度进行分析，并以 JSON 格式返回：
    //             1. theme: 主题思想
    //             2. style: 写作风格
    //             3. interpretation: 诗歌赏析
    //             4. culturalContext: 文化背景
    //             5. literaryDevices: 写作手法 (数组)
    //             6. emotions: 情感特征 (数组)
    //         """.trimIndent();
    protected fun createAnalysisPrompt(poem: Poem_entity): String = """
        请分析这首诗：
        《${poem.title}》 - ${poem.author}
        ${poem.content}
        
        请从以下维度进行分析，并以JSON格式直接返回（不要包含```json```标记）：
        
        {
          "coreTheme": "核心主题，10-15字",
          "essenceStyle": "风格精髓，5-10字",
          "keyEmotions": ["核心情感，3个关键词"],
          "highlightTechniques": ["高亮技法，最多3个"],
          "primaryEmotions": [
            {"emotion": "情感1", "intensity": 0.5},
            {"emotion": "情感2", "intensity": 0.3},
            {"emotion": "情感3", "intensity": 0.2}
          ],
          "culturalContext": "文化背景，50字左右",
          "deepInterpretation": "深度解读，100字左右"
        }
        
        注意：
        1. 直接返回JSON，不需要任何解释或前导文字。
        2. primaryEmotions中必须包含3个主要情感，每个情感都需要有一个名称(emotion)和一个强度值(intensity)。
        3. 所有情感强度值(intensity)加起来必须等于1.0，表示情感的相对重要性。
    """.trimIndent()
} 