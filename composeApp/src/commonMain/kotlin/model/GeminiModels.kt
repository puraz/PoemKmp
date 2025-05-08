package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiModels(
    val models: List<ModelInfo>
) {
    @Serializable
    data class ModelInfo(
        val name: String,
        val version: String? = null,
        val displayName: String? = "",
        val description: String? = "",
        val inputTokenLimit: Int? = null,
        val outputTokenLimit: Int? = null,
        @SerialName("supportedGenerationMethods")
        val supportedGenerationMethods: List<String> = emptyList(),
        val temperature: Double? = null,
        val topP: Double? = null,
        val topK: Int? = null,
        val maxTemperature: Double? = null
    )
} 