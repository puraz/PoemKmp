import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // 重要：允许自定义宽度
    ) {
        Surface(
            // 使用 Surface 或 Card 来提供背景和阴影
            modifier = Modifier
                .fillMaxWidth(0.8f) // 或固定宽度 .width(800.dp)
                .fillMaxHeight(0.8f),  // 限制最大高度,
            shape = RoundedCornerShape(6.dp), // 圆角
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("诗词鉴赏", style = MaterialTheme.typography.body2)
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
                // 内容和滚动
                Box(
                    modifier = Modifier
                        .weight(1f) // 占据剩余空间
                        .padding(horizontal = 16.dp), // 左右边距
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

                // 关闭按钮
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