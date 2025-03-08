package viewmodel

import data.PoemRepository
import data.db.Poem_entity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import service.AIService
import service.PoemAnalysis

class PoemAppreciationViewModel(
    private val aiService: AIService,
    private val poemRepository: PoemRepository
) : BaseViewModel() {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    private val _appreciationState = MutableStateFlow<AppreciationState>(AppreciationState.Initial)
    val appreciationState: StateFlow<AppreciationState> = _appreciationState


    fun loadOrAnalyzePoem(poem: Poem_entity) {
        viewModelScope.launch {
            _appreciationState.value = AppreciationState.Loading

            try {
                // 首先检查缓存里有没有赏析内容了
                val cachedAppreciation = poem.appreciation_content
                if (!cachedAppreciation.isNullOrBlank()) {
                    try {
                        val analysis = json.decodeFromString<PoemAnalysis>(cachedAppreciation)
                        _appreciationState.value = AppreciationState.Success(analysis)
                        return@launch
                    } catch (e: Exception) {
                        // If parsing fails, continue to reanalyze
                    }
                }

                // If no cache or parsing failed, get a new analysis
                analyzeAndSave(poem)
            } catch (e: Exception) {
                _appreciationState.value = AppreciationState.Error(e.message ?: "赏析失败")
            }
        }
    }

    fun reanalyzePoem(poem: Poem_entity) {
        viewModelScope.launch {
            _appreciationState.value = AppreciationState.Loading
            try {
                analyzeAndSave(poem)
            } catch (e: Exception) {
                _appreciationState.value = AppreciationState.Error(e.message ?: "重新赏析失败")
            }
        }
    }

    private suspend fun analyzeAndSave(poem: Poem_entity) {
        val analysis = aiService.analyzePoemContent(poem)
        // 将赏析结果序列化为 JSON 字符串
        val appreciationJson = Json.encodeToString(analysis)
        // 保存到数据库
        poemRepository.updateAppreciation(
            poemId = poem.id,
            appreciationContent = appreciationJson,
            updateTime = Clock.System.now().toEpochMilliseconds()
        )
        _appreciationState.value = AppreciationState.Success(analysis)
    }
}

sealed class AppreciationState {
    object Initial : AppreciationState()
    object Loading : AppreciationState()
    data class Success(val analysis: PoemAnalysis) : AppreciationState()
    data class Error(val message: String) : AppreciationState()
}