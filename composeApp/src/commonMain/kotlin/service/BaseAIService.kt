package service

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
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
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
        maxDelayMs: Long = 10000,
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
        你是一个专业的古典诗词专家。请基于用户的查询意图"$query"，分析并理解用户的搜索需求。
        分析维度包括但不限于：
        1. 主题意境
        2. 情感倾向
        3. 写作手法
        4. 意象特征
        5. 文学体裁
        
        请以专业、客观的视角进行分析。
        
        注意：
        1. 响应必须是有效的 JSON 格式
        2. 所有字段都必须提供非空值
        3. 评分必须在 0-1 之间
        4. 匹配原因必须具体且有说服力
    """.trimIndent()
} 