import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("app.cash.sqldelight") version "2.0.1"
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            
            // 数据库
            implementation("app.cash.sqldelight:runtime:2.0.1")
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
            
            // JSON序列化
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            
            // 协程
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            
            // SQLDelight JVM驱动
            implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
        }
    }
}

// 添加 SQLDelight 配置
sqldelight {
    databases {
        create("PoemDatabase") {
            packageName.set("data.db")
            // 指定 SQL 文件所在目录
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.example.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example"
            packageVersion = "1.0.0"
        }
    }
}
