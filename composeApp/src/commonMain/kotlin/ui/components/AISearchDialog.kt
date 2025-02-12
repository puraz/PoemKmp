package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import data.db.Poem_entity
import viewmodel.AISearchViewModel

@Composable
fun AISearchDialog(
    onDismiss: () -> Unit,
    viewModel: AISearchViewModel, // 假设你有一个 AISearchViewModel
    onPoemSelected: (Poem_entity) -> Unit // 假设 Poem 是诗歌的数据类
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(600.dp)
                .height(700.dp)
            // .fillMaxWidth(0.8f)  // 或者 .width(800.dp)
            // .fillMaxHeight(0.8f),
            , shape = RoundedCornerShape(6.dp)
        ) {
            Column {
                // 标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("发现新诗词", style = MaterialTheme.typography.body2)
                }

                // 内容区域 (AISearchPanel)
                Box(
                    modifier = Modifier
                        .weight(1f) // 占据剩余空间
                        .padding(horizontal = 16.dp)
                ) {
                    AISearchPanel(
                        viewModel = viewModel,
                        onPoemSelected = onPoemSelected,
                        modifier = Modifier.fillMaxSize()
                    )
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