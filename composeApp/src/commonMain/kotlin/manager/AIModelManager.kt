package manager

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import data.DatabaseManager

object AIModelManager {
    private lateinit var databaseManager: DatabaseManager
    private val modelChangeListeners = mutableListOf<() -> Unit>()
    private val defaultModel = AIModel.DEEPSEEK

    val currentModel: MutableState<AIModel> = mutableStateOf(defaultModel)

    fun initialize(dbManager: DatabaseManager) {
        databaseManager = dbManager
        // 初始化后从数据库加载设置
        loadSettings()
    }

    private fun loadSettings() {
        try {
            val modelName = getSetting("current_model", defaultModel.name)
            currentModel.value = AIModel.valueOf(modelName)
        } catch (e: Exception) {
            // 如果出现任何错误，使用默认值
            currentModel.value = defaultModel
        }
    }

    enum class AIModel(val displayName: String, val configKey: String) {
        DEEPSEEK("DeepSeek", "deepseek_api_key"),
        // OPENAI("OpenAI", "openai_api_key"),
        // GEMINI("Google Gemini", "gemini_api_key")
    }

    fun setModel(model: AIModel) {
        setSetting("current_model", model.name)
        currentModel.value = model
        // 通知所有监听器
        modelChangeListeners.forEach { it.invoke() }
    }

    fun getApiKey(): String {
        return getSetting(currentModel.value.configKey, "")
    }

    fun setApiKey(apiKey: String) {
        setSetting(currentModel.value.configKey, apiKey)
    }

    private fun getSetting(key: String, defaultValue: String): String {
        if (!::databaseManager.isInitialized) {
            return defaultValue
        }
        return try {
            val aiSetting = databaseManager.settingsQueries
                .selectByKey(key)
                .executeAsOneOrNull()
            aiSetting ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun setSetting(key: String, value: String) {
        if (!::databaseManager.isInitialized) {
            println("数据库管理器未初始化，无法保存设置")
            return
        }
        try {
            databaseManager.settingsQueries.upsertSetting(
                key = key,
                value_ = value,
                update_time = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            println("设置保存失败: ${e.message}")
        }
    }

    // 添加监听器
    fun addModelChangeListener(listener: () -> Unit) {
        modelChangeListeners.add(listener)
    }

    // 移除监听器
    fun removeModelChangeListener(listener: () -> Unit) {
        modelChangeListeners.remove(listener)
    }
} 