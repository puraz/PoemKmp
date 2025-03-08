package service

import data.db.Poem_entity
import kotlinx.serialization.Serializable

interface AIService {
    suspend fun semanticSearch(
        query: String,
        poems: List<Poem_entity>
    ): List<AISearchResult>
    
    suspend fun analyzePoemContent(poem: Poem_entity): PoemAnalysis
}

@Serializable
data class AISearchResult(
    val title: String,
    val content: String,
    val author: String,
    val dynasty: String?,
    val category: String? = null,
    val notes: String? = null,
    val relevanceScore: Double,
    val matchReason: String,
    val isRecommendation: Boolean = false
)

// @Serializable
// data class PoemAnalysis(
//     val theme: String,
//     val style: String,
//     val interpretation: String,
//     val culturalContext: String,
//     val literaryDevices: List<String>,
//     val emotions: List<String>
// )

@Serializable
data class EmotionWithIntensity(
    val emotion: String,
    val intensity: Float // 0.0-1.0范围内的强度值
)

@Serializable
data class PoemAnalysis(
    val coreTheme: String,           // 必展示核心主题
    val essenceStyle: String,        // 必展示风格精髓
    val keyEmotions: List<String>,   // 必展示核心情感
    val highlightTechniques: List<String>, // 高亮技法（最多3个）
    val primaryEmotions: List<EmotionWithIntensity>, // 主要情感（带强度）
    val culturalContext: String,     // 可折叠的文化背景
    val deepInterpretation: String   // 可折叠深度解读
)