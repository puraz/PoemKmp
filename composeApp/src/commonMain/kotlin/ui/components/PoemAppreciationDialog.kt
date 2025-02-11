import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.db.Poem_entity
import service.PoemAnalysis
import viewmodel.AppreciationState
import viewmodel.PoemAppreciationViewModel

@Composable
fun PoemAppreciationDialog(
    poem: Poem_entity,
    onDismiss: () -> Unit,
    viewModel: PoemAppreciationViewModel
) {
    val appreciationState by viewModel.appreciationState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrAnalyzePoem(poem)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("诗词鉴赏")
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
        },
        text = {
            Box(
                modifier = Modifier
                    .width(800.dp)
                    .heightIn(max = 600.dp)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val state = appreciationState) {
                    is AppreciationState.Loading -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    is AppreciationState.Success -> {
                        val scrollState = rememberScrollState()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 600.dp) // 继承外层高度限制
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState) // 外层滚动
                                    .padding(end = 12.dp) // 为滚动条留出空间
                            ) {
                                AppreciationContent(
                                    analysis = state.analysis,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            VerticalScrollbar(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(scrollState) // 共享滚动状态
                            )
                        }
                    }

                    is AppreciationState.Error -> {
                        ErrorContent(
                            message = state.message
                        )
                    }

                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun AppreciationContent(
    analysis: PoemAnalysis,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppreciationSection("主题思想", analysis.theme)
        AppreciationSection("写作风格", analysis.style)
        AppreciationSection("诗歌赏析", analysis.interpretation)
        AppreciationSection("文化背景", analysis.culturalContext)
        AppreciationSection("写作手法", analysis.literaryDevices.joinToString("\n"))
        AppreciationSection("情感特征", analysis.emotions.joinToString("\n"))
    }
}

@Composable
private fun AppreciationSection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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
    }
}