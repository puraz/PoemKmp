package data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

object DesktopDatabaseDriver {
    fun create(): SqlDriver {
        val appDataDir = File(System.getProperty("user.home"), ".poemkmp")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }

        val databasePath = File(appDataDir, "poems.db")

        // 如果目标数据库文件不存在，从资源文件复制
        if (!databasePath.exists()) {
            // 修改资源文件的路径获取方式
            val inputStream = object {}.javaClass.getResourceAsStream("/poems.db")
                ?: DesktopDatabaseDriver::class.java.classLoader.getResourceAsStream("poems.db")
                ?: throw IllegalStateException("预置数据库文件不存在")

            // 复制数据库文件到应用数据目录
            databasePath.outputStream().use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }
        }

        return JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
    }
} 