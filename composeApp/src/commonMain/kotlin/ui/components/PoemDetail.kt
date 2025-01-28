package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
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
    val colors = MaterialTheme.colors
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(colors.background)
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 标题容器
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = poem.title,
                    style = MaterialTheme.typography.h4,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 收藏按钮
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(56.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (poem.is_favorite > 0) Icons.Default.Favorite 
                                 else Icons.Default.FavoriteBorder,
                    contentDescription = if (poem.is_favorite > 0) "取消收藏" else "收藏",
                    tint = if (poem.is_favorite > 0) colors.primary 
                          else colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(36.dp)
                        .padding(2.dp)
                )
            }
        }
        
        // 作者信息
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = poem.author,
                style = MaterialTheme.typography.subtitle1,
                color = colors.onSurface
            )
            poem.dynasty?.let { dynasty ->
                Text(
                    text = " · $dynasty",
                    style = MaterialTheme.typography.subtitle1,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // 诗词内容
        Text(
            text = poem.content,
            style = MaterialTheme.typography.body1,
            color = colors.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        
        // 注释
        poem.notes?.let { notes ->
            Divider(color = colors.onSurface.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "注释：",
                style = MaterialTheme.typography.subtitle2,
                color = colors.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.body2,
                color = colors.onSurface.copy(alpha = 0.8f)
            )
        }
    }
} 