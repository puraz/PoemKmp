package data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import data.db.PoemDatabase
import java.io.File

object DesktopDatabaseDriver {
    fun create(): SqlDriver {
        // 在用户目录下创建应用专用的数据目录
        val appDataDir = File(System.getProperty("user.home"), ".poemkmp")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }

        // 在应用数据目录下存储数据库文件
        val databasePath = File(appDataDir, "poems.db")
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
        
        if (!databasePath.exists()) {
            PoemDatabase.Schema.create(driver)
        }
        
        return driver
    }
} 