package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .padding(24.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = poem.title,
                style = MaterialTheme.typography.h4
            )
            
            // 更大的收藏按钮
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(48.dp)  // 增大按钮尺寸
            ) {
                Icon(
                    imageVector = if (poem.is_favorite > 0) Icons.Default.Favorite 
                                 else Icons.Default.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (poem.is_favorite > 0) MaterialTheme.colors.primary 
                          else MaterialTheme.colors.onSurface,
                    modifier = Modifier.size(32.dp)  // 增大图标尺寸
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 作者信息
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 诗词内容
        Text(
            text = poem.content,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 注释
        poem.notes?.let { notes ->
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "注释：",
                style = MaterialTheme.typography.subtitle2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
        }
    }
} 