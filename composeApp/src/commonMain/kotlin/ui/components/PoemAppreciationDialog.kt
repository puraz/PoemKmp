
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import data.db.Poem_entity
import service.PoemAnalysis
import ui.components.PieChart
import viewmodel.AppreciationState
import viewmodel.PoemAppreciationViewModel

@Composable
fun PoemAppreciationDialog(
    poem: Poem_entity,
    onDismiss: () -> Unit,
    viewModel: PoemAppreciationViewModel
) {
    val appreciationState by viewModel.appreciationState.collectAsState()

    LaunchedEffect(poem.id) {
        viewModel.loadOrAnalyzePoem(poem)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(6.dp)),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colors.surface
        ) {
            Column {
                // Header with title and refresh button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("诗词鉴赏", style = MaterialTheme.typography.h6)
                    if (appreciationState is AppreciationState.Success) {
                        IconButton(
                            onClick = { viewModel.reanalyzePoem(poem) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "重新赏析",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                }

                // Poem information
                /*Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = poem.title,
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "作者：${poem.author}" + (poem.dynasty?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = poem.content,
                        style = MaterialTheme.typography.body1,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }*/

                // Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                // Content area with state handling
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = appreciationState) {
                        is AppreciationState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colors.primary
                                )
                            }
                        }

                        is AppreciationState.Success -> {
                            val scrollState = rememberScrollState()
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                ) {
                                    AppreciationContent(analysis = state.analysis)
                                }
                                VerticalScrollbar(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .fillMaxHeight(),
                                    adapter = rememberScrollbarAdapter(scrollState)
                                )
                            }
                        }

                        is AppreciationState.Error -> {
                            ErrorContent(
                                message = state.message,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {}
                    }
                }

                // Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppreciationContent(
    analysis: PoemAnalysis,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Core Information Section (Always visible)
        ExpandingCard(title = "核心解读", initiallyExpanded = true) {
            Column(modifier = Modifier.padding(16.dp)) {
                KeyValueRow("主题", analysis.coreTheme)
                KeyValueRow("风格", analysis.essenceStyle)
                KeyValueRow("情感基调", analysis.keyEmotions.joinToString(", "))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Deep Analysis Section (Collapsible)
        ExpandingCard(title = "深度解析", initiallyExpanded = true) {
            Column(modifier = Modifier.padding(16.dp)) {
                KeyValueRow("文化背景", analysis.culturalContext)
                KeyValueRow("经典技法", analysis.highlightTechniques.joinToString(" · "))
                Text(
                    text = "意境解读：${analysis.deepInterpretation}",
                    style = MaterialTheme.typography.body1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Emotion visualization (if available)
        if (analysis.primaryEmotions.isNotEmpty()) {
            ExpandingCard(title = "情感分布") {
                val emotionEntries = analysis.primaryEmotions
                    .filter { it.intensity > 0 }  // 例如，过滤掉强度为0的情感
                // 可以在这里添加其他转换

                PieChart(emotions = emotionEntries)
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "分析失败",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* Retry logic would go here */ },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Text("重试")
        }
    }
}

@Composable
private fun ExpandingCard(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = if (isExpanded) "折叠" else "展开",
                    tint = MaterialTheme.colors.primary
                )
            }

            if (isExpanded) {
                Divider()
                content()
            }
        }
    }
}

@Composable
fun KeyValueRow(key: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$key: ",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun KeywordText(text: String, keywords: Set<String>, modifier: Modifier = Modifier) {
    val highlightedText = buildAnnotatedString {
        val parts = text.split(*keywords.toTypedArray())
        var startIndex = 0

        keywords.forEach { keyword ->
            val index = text.indexOf(keyword, startIndex)
            if (index >= 0) {
                // Add text before keyword
                append(text.substring(startIndex, index))

                // Add highlighted keyword
                withStyle(
                    style = MaterialTheme.typography.body1.toSpanStyle().copy(
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(keyword)
                }

                startIndex = index + keyword.length
            }
        }

        // Add remaining text
        if (startIndex < text.length) {
            append(text.substring(startIndex))
        }
    }

    Text(text = highlightedText, modifier = modifier)
}