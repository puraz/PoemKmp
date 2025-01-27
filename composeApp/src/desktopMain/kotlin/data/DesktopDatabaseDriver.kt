package data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import data.db.PoemDatabase
import java.io.File

object DesktopDatabaseDriver {
    fun create(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), "poemkmp.db")
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
        
        if (!databasePath.exists()) {
            PoemDatabase.Schema.create(driver)
        }
        
        return driver
    }
} 