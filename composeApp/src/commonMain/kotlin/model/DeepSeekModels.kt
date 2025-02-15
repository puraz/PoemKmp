package model

import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekModels(
    val `object`: String,
    val data: List<ModelInfo>
) {
    @Serializable
    data class ModelInfo(
        val id: String,
        val `object`: String,
        val owned_by: String
    )
}