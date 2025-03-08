package theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import data.DatabaseManager

object ThemeManager {

    private lateinit var databaseManager: DatabaseManager
    private const val KEY_THEME_MODE = "theme_mode"

    fun initialize(dbManager: DatabaseManager) {
        databaseManager = dbManager
        loadThemeModeFromDb()
    }

    private fun loadThemeModeFromDb() {
        if (!::databaseManager.isInitialized) {
            return
        }
        try {
            val savedThemeModeName =
                databaseManager.settingsQueries.selectByKey(KEY_THEME_MODE).executeAsOneOrNull()

            if (savedThemeModeName != null) {
                _themeMode.value = ThemeMode.valueOf(savedThemeModeName)
            }
        } catch (e: Exception) {
            println("加载主题模式失败: ${e.message}")
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        if (::databaseManager.isInitialized) {
            try {
                databaseManager.settingsQueries.upsertSetting(
                    key = KEY_THEME_MODE,
                    value_ = mode.name,
                    update_time = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                println("保存模型设置失败: ${e.message}")
            }
            databaseManager.settingsQueries.upsertSetting(
                KEY_THEME_MODE,
                mode.name,
                update_time = System.currentTimeMillis()
            )
        }
    }

    enum class ThemeMode {
        LIGHT,
        DARK,
        SYSTEM
    }

    private val _themeMode = mutableStateOf(ThemeMode.SYSTEM)
    val themeMode: State<ThemeMode> = _themeMode

    // 系统是否处于暗色模式
    private val _isSystemInDarkTheme = mutableStateOf(false)

    // 当前是否应该使用暗色主题
    val isDarkTheme: Boolean
        get() =
            when (themeMode.value) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> _isSystemInDarkTheme.value
            }

    fun setSystemDarkTheme(isDark: Boolean) {
        _isSystemInDarkTheme.value = isDark
    }
}
