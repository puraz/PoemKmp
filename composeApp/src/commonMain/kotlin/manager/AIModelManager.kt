package manager

import data.DatabaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AIModelManager {
    private lateinit var databaseManager: DatabaseManager
    private val configChangeListeners = mutableListOf<() -> Unit>()

    // 统一使用 OpenAI 兼容配置键
    private const val KEY_API_KEY = "api_key_openai"
    private const val KEY_BASE_URL = "base_url_openai"
    private const val KEY_MODEL_VERSION = "model_version_openai"

    // 模型配置数据类
    data class ModelConfig(
        val apiKey: String = "",
        val baseUrl: String = "",
        val modelVersion: String = ""
    )

    // 当前配置
    private val _currentConfig = MutableStateFlow(ModelConfig())
    val currentConfig: StateFlow<ModelConfig> = _currentConfig.asStateFlow()

    fun initialize(dbManager: DatabaseManager) {
        databaseManager = dbManager
        // 初始化后从数据库加载设置
        loadSettings()
    }

    private fun loadSettings() {
        if (!::databaseManager.isInitialized) {
            _currentConfig.value = ModelConfig()
            return
        }

        try {
            val apiKey = databaseManager.settingsQueries.selectByKey(KEY_API_KEY).executeAsOneOrNull()
            val baseUrl = databaseManager.settingsQueries.selectByKey(KEY_BASE_URL).executeAsOneOrNull()
            val modelVersion = databaseManager.settingsQueries.selectByKey(KEY_MODEL_VERSION).executeAsOneOrNull()

            _currentConfig.value = ModelConfig(
                apiKey = apiKey ?: "",
                baseUrl = baseUrl ?: "",
                modelVersion = modelVersion ?: ""
            )
        } catch (e: Exception) {
            println("加载 AI 配置失败: ${e.message}")
            _currentConfig.value = ModelConfig()
        }
    }

    // 获取当前配置
    fun getConfig(): ModelConfig = _currentConfig.value

    // 保存并设置配置
    fun setConfig(config: ModelConfig) {
        _currentConfig.value = config

        if (!::databaseManager.isInitialized) {
            println("数据库管理器未初始化，无法保存配置")
            return
        }

        try {
            val now = System.currentTimeMillis()
            databaseManager.settingsQueries.upsertSetting(KEY_API_KEY, config.apiKey, now)
            databaseManager.settingsQueries.upsertSetting(KEY_BASE_URL, config.baseUrl, now)
            databaseManager.settingsQueries.upsertSetting(KEY_MODEL_VERSION, config.modelVersion, now)
        } catch (e: Exception) {
            println("保存 AI 配置失败: ${e.message}")
        }

        // 通知所有监听器
        configChangeListeners.forEach { it.invoke() }
    }

    // 添加监听器
    fun addConfigChangeListener(listener: () -> Unit) {
        configChangeListeners.add(listener)
    }

    // 移除监听器
    fun removeConfigChangeListener(listener: () -> Unit) {
        configChangeListeners.remove(listener)
    }
}
