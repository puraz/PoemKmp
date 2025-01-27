package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.db.Poem_entity

@Composable
fun PoemDetail(
    poem: Poem_entity,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)  // 减小外边距，让内容区域更大
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),  // 增加底部间距
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 标题容器，使用权重让它自适应宽度
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)  // 与收藏按钮保持间距
            ) {
                Text(
                    text = poem.title,
                    style = MaterialTheme.typography.h4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 收藏按钮，固定尺寸
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(56.dp)  // 增大按钮的基础尺寸
                    .padding(4.dp)  // 内边距，让点击区域更大
            ) {
                Icon(
                    imageVector = if (poem.is_favorite > 0) Icons.Default.Favorite 
                                 else Icons.Default.FavoriteBorder,
                    contentDescription = if (poem.is_favorite > 0) "取消收藏" else "收藏",
                    tint = if (poem.is_favorite > 0) MaterialTheme.colors.primary 
                          else MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .size(36.dp)  // 增大图标尺寸
                        .padding(2.dp)  // 图标内边距
                )
            }
        }
        
        // 作者信息
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),  // 增加底部间距
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = poem.author,
                style = MaterialTheme.typography.subtitle1
            )
            poem.dynasty?.let { dynasty ->
                Text(
                    text = " · $dynasty",
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // 诗词内容
        Text(
            text = poem.content,
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)  // 增加底部间距
        )
        
        // 注释
        poem.notes?.let { notes ->
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "注释：",
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
        }
    }
} 