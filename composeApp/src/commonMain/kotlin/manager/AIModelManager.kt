package manager

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import data.DatabaseManager

object AIModelManager {
    private lateinit var databaseManager: DatabaseManager

    fun initialize(dbManager: DatabaseManager) {
        databaseManager = dbManager
    }

    enum class AIModel(val displayName: String, val configKey: String) {
        DEEPSEEK("DeepSeek", "deepseek_api_key"),
        OPENAI("OpenAI", "openai_api_key"),
        GEMINI("Google Gemini", "gemini_api_key")
    }

    private val defaultModel = AIModel.DEEPSEEK

    val currentModel: MutableState<AIModel> = mutableStateOf(defaultModel)

    init {
        // 初始化时从数据库加载当前模型设置
        try {
            val modelName = getSetting("current_model", defaultModel.name)
            currentModel.value = AIModel.valueOf(modelName)
        } catch (e: Exception) {
            // 如果出现任何错误，使用默认值
            currentModel.value = defaultModel
        }
    }

    fun setModel(model: AIModel) {
        setSetting("current_model", model.name)
        currentModel.value = model
    }

    fun getApiKey(): String {
        return getSetting(currentModel.value.configKey, "")
    }

    fun setApiKey(apiKey: String) {
        setSetting(currentModel.value.configKey, apiKey)
    }

    private fun getSetting(key: String, defaultValue: String): String {
        return try {
            databaseManager.settingsQueries
                .selectByKey(key)
                .executeAsOneOrNull()
                ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun setSetting(key: String, value: String) {
        try {
            databaseManager.settingsQueries.upsertSetting(
                key = key,
                value_ = value,
                update_time = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            // 可以添加错误日志或处理
            println("设置保存失败: ${e.message}")
        }
    }
} 