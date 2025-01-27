package data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import data.db.PoemDatabase
import data.db.Poem_entity
import data.db.Tag_entity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class DatabaseManager(private val driver: SqlDriver) {
    private val database = PoemDatabase(driver)
    private val poemQueries = database.poemEntityQueries
    private val tagQueries = database.tagEntityQueries

    // 诗词相关操作
    fun getAllPoems(): Flow<List<Poem_entity>> =
        poemQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun getPoemById(id: Long): Poem_entity? =
        poemQueries.selectById(id)
            .executeAsOneOrNull()

    suspend fun insertPoem(
        title: String,
        content: String,
        author: String,
        dynasty: String?,
        category: String,
        notes: String?,
        isFavorite: Boolean = false
    ) {
        val currentTime = System.currentTimeMillis()
        poemQueries.insertPoetry(
            title = title,
            content = content,
            author = author,
            dynasty = dynasty,
            category = category,
            create_time = currentTime,
            update_time = currentTime,
            notes = notes,
            is_favorite = if (isFavorite) 1L else 0L
        )
    }

    // 标签相关操作
    fun getTagsForPoem(poemId: Long): Flow<List<Tag_entity>> =
        tagQueries.getTagsForPoetry(poemId)
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun addTagToPoem(poemId: Long, tagName: String) {
        database.transaction {
            // 先插入标签（如果不存在）
            tagQueries.insertTag(tagName)
            // 获取标签ID
            val tagId = tagQueries.selectByName(tagName).executeAsOne().id
            // 添加关联
            tagQueries.addTagToPoetry(poemId, tagId)
        }
    }
} 