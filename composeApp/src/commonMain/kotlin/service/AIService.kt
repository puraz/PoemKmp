package service

import data.db.Poem_entity

interface AIService {
    suspend fun semanticSearch(
        query: String,
        poems: List<Poem_entity>
    ): List<AISearchResult>
    
    suspend fun analyzePoemContent(poem: Poem_entity): PoemAnalysis
}

data class AISearchResult(
    val title: String,
    val content: String,
    val author: String,
    val dynasty: String?,
    val category: String?,
    val notes: String?,
    val relevanceScore: Double,
    val matchReason: String
)

data class PoemAnalysis(
    val theme: String,
    val style: String,
    val interpretation: String,
    val culturalContext: String,
    val literaryDevices: List<String>,
    val emotions: List<String>
) 