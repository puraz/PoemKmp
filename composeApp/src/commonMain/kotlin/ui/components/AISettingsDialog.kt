package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import manager.AIModelManager
import model.GeminiModels
import service.DeepSeekAIService
import service.GeminiAIService

@Composable
fun AISettingsDialog(
    onDismiss: () -> Unit
) {
    var selectedModel by remember { mutableStateOf(AIModelManager.currentModel.value) }
    var modelConfig by remember { 
        mutableStateOf(AIModelManager.getModelConfig(selectedModel))
    }
    var availableVersions by remember { mutableStateOf<List<String>>(emptyList()) }
    var geminiModels by remember { mutableStateOf<List<GeminiModels.ModelInfo>>(emptyList()) }
    var isLoadingVersions by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // 创建一个协程作用域
    val scope = rememberCoroutineScope()

    // 加载可用的模型版本
    LaunchedEffect(selectedModel, modelConfig.apiKey) {
        if (modelConfig.apiKey.isNotBlank()) {
            isLoadingVersions = true
            try {
                scope.launch {
                    when (selectedModel) {
                        AIModelManager.AIModel.DEEPSEEK -> {
                            val service = DeepSeekAIService(
                                apiKey = modelConfig.apiKey,
                                baseUrl = modelConfig.baseUrl,
                                modelVersion = modelConfig.modelVersion
                            )
                            availableVersions = service.getAvailableModels()
                            if (modelConfig.modelVersion !in availableVersions && availableVersions.isNotEmpty()) {
                                modelConfig = modelConfig.copy(modelVersion = availableVersions.first())
                            }
                        }

                        AIModelManager.AIModel.GEMINI -> {
                            val service = GeminiAIService(
                                apiKey = modelConfig.apiKey,
                                baseUrl = modelConfig.baseUrl,
                                modelVersion = modelConfig.modelVersion
                            )
                            geminiModels = service.getAvailableModels()
                            val modelNames = geminiModels.map { it.name.removePrefix("models/") }
                            if (modelConfig.modelVersion !in modelNames && modelNames.isNotEmpty()) {
                                modelConfig = modelConfig.copy(modelVersion = modelNames.first())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("加载模型版本失败: ${e.message}")
            } finally {
                isLoadingVersions = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI 模型设置") },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // 模型选择
                Text(
                    text = "选择 AI 模型",
                    style = MaterialTheme.typography.subtitle1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AIModelManager.AIModel.values().forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedModel == model,
                            onClick = {
                                selectedModel = model
                                modelConfig = AIModelManager.getModelConfig(model)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(model.displayName)
                    }
                }
                
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

                // Base URL 输入
                OutlinedTextField(
                    value = modelConfig.baseUrl,
                    onValueChange = { modelConfig = modelConfig.copy(baseUrl = it) },
                    label = { Text("API 基础链接") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 模型版本选择
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = when (selectedModel) {
                            AIModelManager.AIModel.DEEPSEEK -> modelConfig.modelVersion
                            AIModelManager.AIModel.GEMINI -> {
                                geminiModels.find { it.name.removePrefix("models/") == modelConfig.modelVersion }
                                    ?.displayName ?: modelConfig.modelVersion
                            }
                        },
                        onValueChange = { },
                        label = { Text("模型版本") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "选择模型版本"
                                )
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
                        if (isLoadingVersions) {
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
                            when (selectedModel) {
                                AIModelManager.AIModel.DEEPSEEK -> {
                                    availableVersions.forEach { version ->
                                        DropdownMenuItem(
                                            onClick = {
                                                modelConfig = modelConfig.copy(modelVersion = version)
                                                expanded = false
                                            }
                                        ) {
                                            Text(
                                                text = version,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                AIModelManager.AIModel.GEMINI -> {
                                    geminiModels.asReversed().forEach { model ->
                                        DropdownMenuItem(
                                            onClick = {
                                                modelConfig = modelConfig.copy(
                                                    modelVersion = model.name.removePrefix("models/")
                                                )
                                                expanded = false
                                            }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = model.displayName,
                                                    style = MaterialTheme.typography.subtitle1
                                                )
                                                Text(
                                                    text = model.description,
                                                    style = MaterialTheme.typography.caption,
                                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // API Key 说明
                Text(
                    text = when (selectedModel) {
                        AIModelManager.AIModel.DEEPSEEK -> "DeepSeek API Key 获取方式：访问 https://platform.deepseek.com"
                        AIModelManager.AIModel.GEMINI -> "Gemini API Key 获取方式：访问 https://ai.google.dev"
                    },
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    AIModelManager.saveModelConfig(selectedModel, modelConfig)
                    AIModelManager.setModel(selectedModel)
                    onDismiss()
                },
                enabled = modelConfig.apiKey.isNotBlank()
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