package manager

import data.DatabaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AIModelManager {
    private lateinit var databaseManager: DatabaseManager
    private val modelChangeListeners = mutableListOf<() -> Unit>()
    private val defaultModel = AIModel.DEEPSEEK

    enum class AIModel(val displayName: String) {
        DEEPSEEK("DeepSeek"),
        GEMINI("Gemini")
    }

    // 当前选中的模型
    private val _currentModel = MutableStateFlow(AIModel.DEEPSEEK)
    val currentModel: StateFlow<AIModel> = _currentModel.asStateFlow()

    // 模型配置数据类
    data class ModelConfig(
        val apiKey: String = "",
        val baseUrl: String = "",
        val modelVersion: String = ""
    )

    // 存储在数据库中的键名前缀
    private const val KEY_PREFIX_API_KEY = "api_key_"
    private const val KEY_PREFIX_BASE_URL = "base_url_"
    private const val KEY_PREFIX_MODEL_VERSION = "model_version_"

    fun initialize(dbManager: DatabaseManager) {
        databaseManager = dbManager
        // 初始化后从数据库加载设置
        loadSettings()
    }

    private fun loadSettings() {
        try {
            val modelName = getSetting("current_model", defaultModel.name)
            _currentModel.value = AIModel.valueOf(modelName)
        } catch (e: Exception) {
            // 如果出现任何错误，使用默认值
            _currentModel.value = defaultModel
        }
    }

    // 获取特定模型的配置
    fun getModelConfig(model: AIModel): ModelConfig {
        if (!::databaseManager.isInitialized) {
            return ModelConfig(
                baseUrl = getDefaultBaseUrl(model),
                modelVersion = getDefaultModelVersion(model)
            )
        }

        val (apiKey, baseUrl, modelVersion) = databaseManager.getModelConfig(model.name)
        return ModelConfig(
            apiKey = apiKey ?: "",
            baseUrl = baseUrl ?: getDefaultBaseUrl(model),
            modelVersion = modelVersion ?: getDefaultModelVersion(model)
        )
    }

    // 保存模型配置
    fun saveModelConfig(model: AIModel, config: ModelConfig) {
        if (!::databaseManager.isInitialized) {
            println("数据库管理器未初始化，无法保存配置")
            return
        }

        try {
            databaseManager.saveModelConfig(
                modelName = model.name,
                apiKey = config.apiKey,
                baseUrl = config.baseUrl,
                modelVersion = config.modelVersion
            )
        } catch (e: Exception) {
            println("配置保存失败: ${e.message}")
        }
    }

    // 设置当前模型
    fun setModel(model: AIModel) {
        _currentModel.value = model
        // 通知所有监听器
        modelChangeListeners.forEach { it.invoke() }
    }

    // 获取默认的基础URL
    private fun getDefaultBaseUrl(model: AIModel): String = when (model) {
        AIModel.DEEPSEEK -> "https://api.deepseek.com/v1"
        AIModel.GEMINI -> "https://generativelanguage.googleapis.com/v1"
    }

    // 获取默认的模型版本
    private fun getDefaultModelVersion(model: AIModel): String = when (model) {
        AIModel.DEEPSEEK -> "deepseek-chat"
        AIModel.GEMINI -> "gemini-pro"
    }

    fun getApiKey(): String {
        return getSetting(currentModel.value.name, "")
    }

    fun setApiKey(apiKey: String) {
        setSetting(currentModel.value.name, apiKey)
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