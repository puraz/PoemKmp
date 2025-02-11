package data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import data.db.Poem_entity as PoemEntity

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

    suspend fun getFavoritePoems(): Flow<List<PoemEntity>> =
        databaseManager.getFavoritePoems()

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        databaseManager.toggleFavorite(id, isFavorite)
    }

    suspend fun updateAppreciation(
        poemId: Long,
        appreciationContent: String,
        updateTime: Long
    ) {
        withContext(Dispatchers.IO) {
            databaseManager.updateAppreciation(
                appreciationContent = appreciationContent,
                updateTime = updateTime,
                id = poemId
            )
        }
    }
} 