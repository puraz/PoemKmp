package ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
            .padding(start = 10.dp, end = 10.dp, bottom = 40.dp, top = 0.dp)
            .background(colors.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .align(Alignment.Center)
            ) {
                SelectionContainer {
                    Text(
                        text = poem.title,
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            lineHeight = 1.4.em
                        ),
                        color = colors.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = if (poem.is_favorite > 0) Icons.Default.Favorite 
                                 else Icons.Default.FavoriteBorder,
                    contentDescription = if (poem.is_favorite > 0) "取消收藏" else "收藏",
                    tint = if (poem.is_favorite > 0) colors.primary 
                          else colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // 作者信息
        Row(
            modifier = Modifier
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SelectionContainer {
                Row {
                    Text(
                        text = poem.author,
                        style = MaterialTheme.typography.subtitle1.copy(
                            letterSpacing = 1.sp
                        ),
                        color = colors.onSurface
                    )
                    poem.dynasty?.let { dynasty ->
                        Text(
                            text = " · $dynasty",
                            style = MaterialTheme.typography.subtitle1,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // 诗词内容
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            elevation = 0.dp,
            backgroundColor = colors.surface.copy(alpha = 0.5f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    SelectionContainer {
                        Column {
                            Text(
                                text = poem.content,
                                style = MaterialTheme.typography.body1.copy(
                                    lineHeight = 1.7.em,
                                    letterSpacing = 0.5.sp
                                ),
                                color = colors.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 5.dp),
                                textAlign = TextAlign.Center
                            )

                            // 注释部分
                            poem.notes?.let { notes ->
                                Spacer(modifier = Modifier.height(32.dp))
                                Divider(
                                    color = colors.onSurface.copy(alpha = 0.08f),
                                    modifier = Modifier.fillMaxWidth(),
                                    thickness = 1.dp
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "注释",
                                        style = MaterialTheme.typography.subtitle2.copy(
                                            letterSpacing = 2.sp
                                        ),
                                        color = colors.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    Text(
                                        text = notes,
                                        style = MaterialTheme.typography.body2.copy(
                                            lineHeight = 1.8.em
                                        ),
                                        color = colors.onSurface.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState)
                )
            }
        }
    }
} 