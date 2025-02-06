package viewmodel

import data.db.Poem_entity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import service.AIService
import service.PoemAnalysis

class PoemAppreciationViewModel(
    private val aiService: AIService
) : BaseViewModel() {
    private val _appreciationState = MutableStateFlow<AppreciationState>(AppreciationState.Initial)
    val appreciationState = _appreciationState.asStateFlow()

    fun analyzePoem(poem: Poem_entity) {
        viewModelScope.launch {
            _appreciationState.value = AppreciationState.Loading
            try {
                val analysis = aiService.analyzePoemContent(poem)
                _appreciationState.value = AppreciationState.Success(analysis)
            } catch (e: Exception) {
                _appreciationState.value = AppreciationState.Error(e.message ?: "分析失败")
            }
        }
    }
}

sealed class AppreciationState {
    object Initial : AppreciationState()
    object Loading : AppreciationState()
    data class Success(val analysis: PoemAnalysis) : AppreciationState()
    data class Error(val message: String) : AppreciationState()
}