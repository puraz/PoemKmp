package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
    
    Column(modifier = modifier) {
        // 主题设置按钮
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("主题设置")
                Text(
                    when (ThemeManager.themeMode.value) {
                        ThemeManager.ThemeMode.LIGHT -> "浅色"
                        ThemeManager.ThemeMode.DARK -> "深色"
                        ThemeManager.ThemeMode.SYSTEM -> "跟随系统"
                    }
                )
            }
        }
        
        // 使用 DropdownMenu 的替代方案：PopupMenu
        if (expanded) {
            Popup(
                onDismissRequest = { expanded = false },
                alignment = Alignment.TopEnd
            ) {
                Surface(
                    elevation = 8.dp,
                    modifier = Modifier.width(IntrinsicSize.Min)
                ) {
                    Column {
                        ThemeManager.ThemeMode.values().forEach { mode ->
                            TextButton(
                                onClick = {
                                    ThemeManager.setThemeMode(mode)
                                    expanded = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    when (mode) {
                                        ThemeManager.ThemeMode.LIGHT -> "浅色"
                                        ThemeManager.ThemeMode.DARK -> "深色"
                                        ThemeManager.ThemeMode.SYSTEM -> "跟随系统"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 