package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import manager.AIModelManager

@Composable
fun AISettingsDialog(
    onDismiss: () -> Unit
) {
    var selectedModel by remember { mutableStateOf(AIModelManager.currentModel.value) }
    var modelConfig by remember {
        mutableStateOf(AIModelManager.getModelConfig(selectedModel))
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

                // 模型版本输入
                OutlinedTextField(
                    value = modelConfig.modelVersion,
                    onValueChange = { modelConfig = modelConfig.copy(modelVersion = it) },
                    label = { Text("模型版本") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
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