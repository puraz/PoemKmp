package service

sealed class AIServiceException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(message: String, cause: Throwable? = null) : 
        AIServiceException("网络错误: $message", cause)
    
    class APIError(val code: Int, message: String) : 
        AIServiceException("API错误 ($code): $message")
    
    class ResponseParsingError(message: String, cause: Throwable? = null) : 
        AIServiceException("响应解析错误: $message", cause)
    
    class InvalidResponseError(message: String) : 
        AIServiceException("无效响应: $message")
    
    class RateLimitError(message: String) : 
        AIServiceException("请求频率限制: $message")
    
    class AuthenticationError(message: String) : 
        AIServiceException("认证错误: $message")
} 