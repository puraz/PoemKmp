package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.db.Poem_entity

@Composable
fun PoemEditDialog(
    poem: Poem_entity? = null,  // null 表示新增，非 null 表示编辑
    onDismiss: () -> Unit,
    onConfirm: (PoemData) -> Unit
) {
    var title by remember { mutableStateOf(poem?.title ?: "") }
    var content by remember { mutableStateOf(poem?.content ?: "") }
    var author by remember { mutableStateOf(poem?.author ?: "") }
    var dynasty by remember { mutableStateOf(poem?.dynasty ?: "") }
    var category by remember { mutableStateOf(poem?.category ?: "") }
    var notes by remember { mutableStateOf(poem?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (poem == null) "添加诗词" else "编辑诗词",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("内容") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text("作者") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = dynasty,
                            onValueChange = { dynasty = it },
                            label = { Text("朝代") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("注释") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }

                    TextButton(
                        onClick = {
                            onConfirm(
                                PoemData(
                                    title = title,
                                    content = content,
                                    author = author,
                                    dynasty = dynasty.takeIf { it.isNotBlank() },
                                    category = category,
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                            )
                        },
                        enabled = title.isNotBlank() && content.isNotBlank() &&
                                author.isNotBlank()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

data class PoemData(
    val title: String,
    val content: String,
    val author: String,
    val dynasty: String?,
    val category: String,
    val notes: String?
) 