package data

import kotlinx.coroutines.flow.Flow
import data.db.Poem_entity as PoemEntity
import data.db.Tag_entity as TagEntity

class PoemRepository(private val databaseManager: DatabaseManager) {
    fun getAllPoems(): Flow<List<PoemEntity>> =
        databaseManager.getAllPoems()

    suspend fun getPoemById(id: Long): PoemEntity? =
        databaseManager.getPoemById(id)

    suspend fun addPoem(
        title: String,
        content: String,
        author: String,
        dynasty: String?,
        category: String,
        notes: String?,
        tags: List<String> = emptyList()
    ) {
        databaseManager.insertPoem(
            title = title,
            content = content,
            author = author,
            dynasty = dynasty,
            category = category,
            notes = notes
        )
    }

    suspend fun updatePoem(
        id: Long,
        title: String,
        content: String,
        author: String,
        dynasty: String?,
        category: String,
        notes: String?,
        isFavorite: Boolean = false
    ) {
        databaseManager.updatePoem(
            id = id,
            title = title,
            content = content,
            author = author,
            dynasty = dynasty,
            category = category,
            notes = notes,
            isFavorite = isFavorite
        )
    }

    suspend fun deletePoem(id: Long) {
        databaseManager.deletePoem(id)
    }

    fun getTagsForPoem(poemId: Long): Flow<List<TagEntity>> =
        databaseManager.getTagsForPoem(poemId)

    suspend fun addTagToPoem(poemId: Long, tagName: String) {
        databaseManager.addTagToPoem(poemId, tagName)
    }

    suspend fun getFavoritePoems(): Flow<List<PoemEntity>> =
        databaseManager.getFavoritePoems()

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        databaseManager.toggleFavorite(id, isFavorite)
    }
} 