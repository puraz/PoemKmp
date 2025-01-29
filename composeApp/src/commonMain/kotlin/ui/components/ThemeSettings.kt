package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import theme.ThemeManager

@Composable
fun ThemeSettings(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        SettingsItem(
            icon = Icons.Default.Palette,
            title = "主题设置",
            value = when (ThemeManager.themeMode.value) {
                ThemeManager.ThemeMode.LIGHT -> "浅色"
                ThemeManager.ThemeMode.DARK -> "深色"
                ThemeManager.ThemeMode.SYSTEM -> "跟随系统"
            },
            onClick = { expanded = true }
        )
        
        if (expanded) {
            Popup(
                onDismissRequest = { expanded = false },
                alignment = Alignment.TopEnd
            ) {
                Surface(
                    elevation = 8.dp,
                    modifier = Modifier.width(IntrinsicSize.Min)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        ThemeManager.ThemeMode.values().forEach { mode ->
                            TextButton(
                                onClick = {
                                    ThemeManager.setThemeMode(mode)
                                    expanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = when (mode) {
                                        ThemeManager.ThemeMode.LIGHT -> "浅色"
                                        ThemeManager.ThemeMode.DARK -> "深色"
                                        ThemeManager.ThemeMode.SYSTEM -> "跟随系统"
                                    },
                                    color = if (mode == ThemeManager.themeMode.value)
                                        MaterialTheme.colors.primary
                                    else
                                        MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 