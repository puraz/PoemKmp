package ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import service.AISearchResult

@Composable
fun AISearchResultCard(
    result: AISearchResult,
    onAddToSystem: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题和作者
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = result.title,
                            style = MaterialTheme.typography.h6
                        )
                        if (result.isRecommendation) {
                            Surface(
                                color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "AI 推荐",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${result.author} ${result.dynasty ?: ""}",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                IconButton(
                    onClick = onAddToSystem,
                    modifier = Modifier.size(48.dp)
                ) {
                    androidx.compose.material.Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加到系统",
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 诗词内容
            Text(
                text = result.content,
                style = MaterialTheme.typography.body1,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 匹配原因
            Text(
                text = "匹配原因：${result.matchReason}",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary
            )
            
            // 相关度
            LinearProgressIndicator(
                progress = result.relevanceScore.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

private fun RowScope.Icon(
    imageVector: Any,
    contentDescription: kotlin.String,
    tint: androidx.compose.ui.graphics.Color
) {
}
