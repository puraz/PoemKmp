package data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import data.db.PoemDatabase
import java.io.File

object DesktopDatabaseDriver {
    fun create(): SqlDriver {
        val appDataDir = File(System.getProperty("user.home"), ".poemkmp")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }

        val databasePath = File(appDataDir, "poems.db")

        // 如果数据库文件不存在
        if (!databasePath.exists()) {
            // 尝试从资源文件复制
            val inputStream = object {}.javaClass.getResourceAsStream("/poems.db")
                ?: DesktopDatabaseDriver::class.java.classLoader.getResourceAsStream("poems.db")

            if (inputStream != null) {
                // 如果存在预置数据库，复制到应用数据目录
                databasePath.outputStream().use { output ->
                    inputStream.use { input ->
                        input.copyTo(output)
                    }
                }
            } else {
                // 如果没有预置数据库，创建新的数据库文件
                createNewDatabase(databasePath)
            }
        }

        return JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
    }

    private fun createNewDatabase(databasePath: File) {
        // 创建空的 SQLite 数据库文件
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")

        // 创建数据库架构
        PoemDatabase.Schema.create(driver)

        // 初始化基础数据
        initializeBasicData(driver)
    }

    private fun initializeBasicData(driver: SqlDriver) {
        val jdbcDriver = driver as JdbcSqliteDriver

        // 读取 default.sql 文件
        val defaultSql = object {}.javaClass.getResourceAsStream("/default.sql")?.bufferedReader()?.readText()
            ?: throw IllegalStateException("无法读取 default.sql 文件")

        // 执行 SQL 语句
        jdbcDriver.getConnection().createStatement().use { statement ->
            // 分割多个 SQL 语句（假设语句以分号结尾）
            defaultSql.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { sql ->
                    statement.executeUpdate(sql)
                }
        }
    }
} 