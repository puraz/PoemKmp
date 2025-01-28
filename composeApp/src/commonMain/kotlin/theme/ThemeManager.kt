package theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object ThemeManager {
    // 主题模式枚举
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
    
    private val _themeMode = mutableStateOf(ThemeMode.SYSTEM)
    val themeMode: State<ThemeMode> = _themeMode
    
    // 系统是否处于暗色模式
    private val _isSystemInDarkTheme = mutableStateOf(false)
    
    // 当前是否应该使用暗色主题
    val isDarkTheme: Boolean
        get() = when (themeMode.value) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> _isSystemInDarkTheme.value
        }
    
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
    
    fun setSystemDarkTheme(isDark: Boolean) {
        _isSystemInDarkTheme.value = isDark
    }
} 