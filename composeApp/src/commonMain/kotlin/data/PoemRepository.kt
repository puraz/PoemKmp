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

    fun getTagsForPoem(poemId: Long): Flow<List<TagEntity>> =
        databaseManager.getTagsForPoem(poemId)

    suspend fun addTagToPoem(poemId: Long, tagName: String) {
        databaseManager.addTagToPoem(poemId, tagName)
    }
} 