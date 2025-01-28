package org.example

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import theme.ThemeManager
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.system.exitProcess

fun main() = application {
    // 使用 Compose 的 isSystemInDarkTheme
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // 监听系统主题变化并更新 ThemeManager
    LaunchedEffect(systemInDarkTheme) {
        println("System is in dark theme: $systemInDarkTheme")
        ThemeManager.setSystemDarkTheme(systemInDarkTheme)
    }
    
    // 原有的窗口代码
    var apiKey by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            apiKey = loadApiKey().also { key ->
                println("API Key 加载状态: ${if (key != null) "成功" else "失败"}")
            }
        } catch (e: Exception) {
            errorMessage = e.message
            println("加载 API Key 时出错: ${e.message}")
        }
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "诗词收藏",
        state = rememberWindowState(size = DpSize(1130.dp, 800.dp))
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                apiKey != null -> {
                    App(
                        deepseekApiKey = apiKey!!
                    )
                }
                else -> {
                    ApiKeyErrorDialog(
                        message = errorMessage ?: "未找到 API Key",
                        onRetry = { 
                            apiKey = loadApiKey()
                        },
                        onExit = {
                            exitProcess(1)
                        }
                    )
                }
            }
        }
    }
}

private fun loadApiKey(): String? {
    val configLocations = listOf(
        "config.properties",  // 项目根目录
        "composeApp/config.properties",  // composeApp 目录
        "${System.getProperty("user.home")}/.poemkmp/config.properties"  // 用户主目录
    )
    
    println("开始查找配置文件...")
    
    for (location in configLocations) {
        val file = File(location)
        if (file.exists()) {
            println("找到配置文件: $location")
            try {
                val properties = Properties()
                FileInputStream(file).use { stream ->
                    properties.load(stream)
                    properties.getProperty("deepseek.api.key")?.let { key ->
                        if (key.isNotBlank() && key != "your_api_key_here") {
                            println("成功从 $location 加载 API Key")
                            return key
                        }
                    }
                }
            } catch (e: Exception) {
                println("读取配置文件 $location 失败: ${e.message}")
            }
        } else {
            println("配置文件不存在: $location")
        }
    }
    
    // 最后尝试从环境变量获取
    System.getenv("DEEPSEEK_API_KEY")?.let { key ->
        if (key.isNotBlank()) {
            println("从环境变量加载 API Key 成功")
            return key
        }
    }
    
    println("未找到有效的 API Key")
    return null
}

@Composable
private fun ApiKeyErrorDialog(
    message: String,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("API Key 配置错误") },
        text = { 
            Column {
                Text("无法找到有效的 DeepSeek API Key。请按以下步骤配置：")
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("方法 1: 创建配置文件")
                Text("在项目根目录或 composeApp 目录下创建 config.properties 文件：")
                Text("deepseek.api.key=your_api_key_here", style = MaterialTheme.typography.caption)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("方法 2: 创建用户级配置")
                Text("创建文件 ~/.poemkmp/config.properties：")
                Text("mkdir -p ~/.poemkmp", style = MaterialTheme.typography.caption)
                Text("echo 'deepseek.api.key=your_api_key_here' > ~/.poemkmp/config.properties", 
                     style = MaterialTheme.typography.caption)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("方法 3: 设置环境变量")
                Text("export DEEPSEEK_API_KEY=your_api_key_here", 
                     style = MaterialTheme.typography.caption)
                
                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("错误信息：$message", color = MaterialTheme.colors.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("重试")
            }
        },
        dismissButton = {
            Button(onClick = onExit) {
                Text("退出")
            }
        }
    )
}