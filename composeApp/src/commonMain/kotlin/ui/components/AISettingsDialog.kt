package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import manager.AIModelManager
import service.OpenAIService

@Composable
fun AISettingsDialog(
    onDismiss: () -> Unit
) {
    // 当前配置（OpenAI 兼容）
    var modelConfig by remember { mutableStateOf(AIModelManager.getConfig()) }
    // 可用模型列表
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    // 是否正在加载模型列表
    var isLoadingModels by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // 加载可用的模型列表
    LaunchedEffect(modelConfig.apiKey, modelConfig.baseUrl) {
        if (modelConfig.apiKey.isBlank() || modelConfig.baseUrl.isBlank()) {
            availableModels = emptyList()
            return@LaunchedEffect
        }

        // 切换配置时先清空旧列表，避免误选
        availableModels = emptyList()
        expanded = false
        isLoadingModels = true
        try {
            val service = OpenAIService(
                apiKey = modelConfig.apiKey,
                baseUrl = modelConfig.baseUrl,
                model = modelConfig.modelVersion
            )
            // 过滤非文本类模型
            val filteredKeywords = listOf(
                "embedding",
                "whisper",
                "tts",
                "moderation",
                "image",
                "audio",
                "vision",
                "realtime"
            )
            availableModels = service.getAvailableModels()
                .filterNot { model ->
                    filteredKeywords.any { keyword ->
                        model.contains(keyword, ignoreCase = true)
                    }
                }
            if (modelConfig.modelVersion !in availableModels && availableModels.isNotEmpty()) {
                modelConfig = modelConfig.copy(modelVersion = availableModels.first())
            }
        } catch (e: Exception) {
            println("加载模型列表失败: ${e.message}")
        } finally {
            isLoadingModels = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI 设置") },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // 模式提示
                Text(
                    text = "OpenAI 兼容模式",
                    style = MaterialTheme.typography.subtitle1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Base URL 输入
                OutlinedTextField(
                    value = modelConfig.baseUrl,
                    onValueChange = { modelConfig = modelConfig.copy(baseUrl = it) },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // API Key 输入
                OutlinedTextField(
                    value = modelConfig.apiKey,
                    onValueChange = { modelConfig = modelConfig.copy(apiKey = it) },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 模型版本选择
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = modelConfig.modelVersion,
                        onValueChange = { modelConfig = modelConfig.copy(modelVersion = it) },
                        label = { Text("模型版本") },
                        modifier = Modifier.fillMaxWidth(),
                        // 支持手动输入 + 下拉选择
                        readOnly = false,
                        trailingIcon = {
                            if (availableModels.isNotEmpty()) {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "选择模型版本"
                                    )
                                }
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .heightIn(max = 300.dp)
                    ) {
                        if (isLoadingModels) {
                            DropdownMenuItem(
                                onClick = { },
                                enabled = false
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else {
                            availableModels.forEach { model ->
                                DropdownMenuItem(
                                    onClick = {
                                        modelConfig = modelConfig.copy(modelVersion = model)
                                        expanded = false
                                    }
                                ) {
                                    Text(
                                        text = model,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    AIModelManager.setConfig(modelConfig)
                    onDismiss()
                },
                enabled = modelConfig.apiKey.isNotBlank() &&
                    modelConfig.baseUrl.isNotBlank() &&
                    modelConfig.modelVersion.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 
