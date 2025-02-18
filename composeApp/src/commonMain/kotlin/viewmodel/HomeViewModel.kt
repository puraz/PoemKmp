package viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import data.PoemRepository
import data.db.Poem_entity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ui.components.PoemData

class HomeViewModel(
    private val repository: PoemRepository
) : BaseViewModel() {
    private val _poems = mutableStateOf<List<Poem_entity>>(emptyList())
    val poems: State<List<Poem_entity>> = _poems.asState()

    private val _selectedPoem = mutableStateOf<Poem_entity?>(null)
    val selectedPoem: State<Poem_entity?> = _selectedPoem.asState()

    private val _showEditDialog = mutableStateOf(false)
    val showEditDialog: State<Boolean> = _showEditDialog.asState()

    private val _poemToEdit = mutableStateOf<Poem_entity?>(null)
    val poemToEdit: State<Poem_entity?> = _poemToEdit.asState()

    init {
        loadPoems()
    }

    private fun loadPoems() {
        viewModelScope.launch {
            repository.getAllPoems().collectLatest { poemList ->
                _poems.value = poemList
                _selectedPoem.value?.let { selected ->
                    _selectedPoem.value = poemList.find { it.id == selected.id }
                }
            }
        }
    }

    fun onPoemSelected(poem: Poem_entity) {
        _selectedPoem.value = poem
    }

    fun onAddPoemClick() {
        _poemToEdit.value = null
        _showEditDialog.value = true
    }

    fun onEditPoemClick(poem: Poem_entity) {
        _poemToEdit.value = poem
        _showEditDialog.value = true
    }

    fun onDeletePoemClick(poem: Poem_entity) {
        viewModelScope.launch {
            repository.deletePoem(poem.id)
        }
    }

    fun onEditDialogDismiss() {
        _showEditDialog.value = false
        _poemToEdit.value = null
    }

    fun onEditDialogConfirm(poemData: PoemData) {
        viewModelScope.launch {
            val poemToEdit = _poemToEdit.value
            if (poemToEdit == null) {
                // 新增
                repository.addPoem(
                    title = poemData.title,
                    content = poemData.content,
                    author = poemData.author,
                    dynasty = poemData.dynasty,
                    category = poemData.category,
                    notes = poemData.notes
                )
            } else {
                // 编辑
                repository.updatePoem(
                    id = poemToEdit.id,
                    title = poemData.title,
                    content = poemData.content,
                    author = poemData.author,
                    dynasty = poemData.dynasty,
                    category = poemData.category,
                    notes = poemData.notes
                )
            }
            _showEditDialog.value = false
            _poemToEdit.value = null
        }
    }

    fun toggleFavorite(poem: Poem_entity) {
        val updatedPoem = poem.copy(
            is_favorite = if (poem.is_favorite == 0L) 1L else 0L
        )
        
        _selectedPoem.value = updatedPoem
        
        _poems.value = _poems.value.map { 
            if (it.id == poem.id) updatedPoem else it 
        }
        
        viewModelScope.launch {
            repository.toggleFavorite(
                id = poem.id,
                isFavorite = updatedPoem.is_favorite == 1L
            )
        }
    }
} 