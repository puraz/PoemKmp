package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.components.*
import viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 诗词列表
            Column(
                modifier = Modifier.weight(0.4f)
                    .fillMaxHeight()
                    .padding(top = 16.dp)
            ) {
                LazyColumn {
                    items(viewModel.poems.value) { poem ->
                        PoemListItem(
                            poem = poem,
                            onClick = { viewModel.onPoemSelected(poem) }
                        )
                    }
                }
            }

            // 诗词详情
            Box(
                modifier = Modifier.weight(0.6f)
                    .fillMaxHeight()
            ) {
                viewModel.selectedPoem.value?.let { poem ->
                    Column {
                        // 操作按钮
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { viewModel.onEditPoemClick(poem) }) {
                                Icon(Icons.Default.Edit, "编辑")
                            }
                            IconButton(onClick = { viewModel.onDeletePoemClick(poem) }) {
                                Icon(Icons.Default.Delete, "删除")
                            }
                        }

                        PoemDetail(
                            poem = poem,
                            onFavoriteClick = { viewModel.toggleFavorite(poem) }
                        )
                    }
                }
            }
        }

        // 添加按钮
        AddPoemFab(
            onClick = viewModel::onAddPoemClick,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // 编辑对话框
        if (viewModel.showEditDialog.value) {
            PoemEditDialog(
                poem = viewModel.poemToEdit.value,
                onDismiss = viewModel::onEditDialogDismiss,
                onConfirm = viewModel::onEditDialogConfirm
            )
        }
    }
}