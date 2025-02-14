package data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import data.db.PoemDatabase
import data.db.Poem_entity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class DatabaseManager(private val driver: SqlDriver) {
    private val database = PoemDatabase(driver)
    private val poemQueries = database.poemEntityQueries
    internal val settingsQueries = database.settingsEntityQueries
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

    suspend fun updatePoem(
        id: Long,
        title: String,
        content: String,
        author: String,
        dynasty: String?,
        category: String,
        notes: String?,
        isFavorite: Boolean
    ) {
        val currentTime = System.currentTimeMillis()
        poemQueries.updatePoetry(
            id = id,
            title = title,
            content = content,
            author = author,
            dynasty = dynasty,
            category = category,
            update_time = currentTime,
            notes = notes,
            is_favorite = if (isFavorite) 1L else 0L
        )
    }

    suspend fun deletePoem(id: Long) {
        poemQueries.deletePoetry(id)
    }

    fun getFavoritePoems(): Flow<List<Poem_entity>> =
        poemQueries.selectFavorites()
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        val currentTime = System.currentTimeMillis()
        poemQueries.updateFavoriteStatus(
            is_favorite = if (isFavorite) 1L else 0L,
            update_time = currentTime,
            id = id
        )
    }

    fun updateAppreciation(
        appreciationContent: String,
        updateTime: Long,
        id: Long
    ) {
        database.poemEntityQueries.updateAppreciation(
            appreciation_content = appreciationContent,
            update_time = updateTime,
            id = id
        )
    }

    // 获取模型配置
    fun getModelConfig(modelName: String): Triple<String?, String?, String?> {
        return Triple(
            settingsQueries.selectByKey("api_key_$modelName").executeAsOneOrNull(),
            settingsQueries.selectByKey("base_url_$modelName").executeAsOneOrNull(),
            settingsQueries.selectByKey("model_version_$modelName").executeAsOneOrNull()
        )
    }

    // 保存模型配置
    fun saveModelConfig(
        modelName: String,
        apiKey: String,
        baseUrl: String,
        modelVersion: String
    ) {
        val currentTime = System.currentTimeMillis()
        database.transaction {
            settingsQueries.upsertSetting("api_key_$modelName", apiKey, currentTime)
            settingsQueries.upsertSetting("base_url_$modelName", baseUrl, currentTime)
            settingsQueries.upsertSetting("model_version_$modelName", modelVersion, currentTime)
        }
    }
} 