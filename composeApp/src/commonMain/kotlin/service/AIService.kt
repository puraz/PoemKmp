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
    val category: String?,
    val notes: String?,
    val relevanceScore: Double,
    val matchReason: String,
    val isRecommendation: Boolean = false
)

@Serializable
data class PoemAnalysis(
    val theme: String,
    val style: String,
    val interpretation: String,
    val culturalContext: String,
    val literaryDevices: List<String>,
    val emotions: List<String>
) 