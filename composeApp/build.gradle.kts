import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.9.0" // Use your Kotlin version
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

            // Ktor client
            implementation("io.ktor:ktor-client-core:2.3.7")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

            // 根据平台选择合适的 HTTP 引擎
            implementation("io.ktor:ktor-client-cio:2.3.7")

            // 添加 multiplatform-settings 依赖
            implementation("com.russhwolf:multiplatform-settings:1.1.1")
            // 如果需要协程支持，也可以添加
            implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.1")

            // Material 图标库
            implementation(compose.materialIconsExtended)
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.AppImage, TargetFormat.Exe)
            packageName = "poemkmp"
            packageVersion = "1.0.0"
            
            // 添加 RPM 特定配置
            linux {
                // RPM 包信息配置
                packageName = "poemkmp"  // RPM 包名
                debMaintainer = "puraz2258@gmail.com"  // 维护者邮箱
                appCategory = "Utility"  // 应用类别
                menuGroup = "Utility"  // 应用程序菜单分组
                
                // 安装位置配置
                installationPath = "/opt/${packageName}"  // 安装路径
                
                // RPM 包元数据
                rpmLicenseType = "Apache 2.0"  // 许可证类型
                rpmPackageVersion = "1.0.0"  // 发布者

                // 图标配置（可选）
                // iconFile.set(project.file("src/main/resources/icon.png"))
            }

            /*windows {
                // Windows 特定配置
                packageName = "诗词收藏"  // 应用名称
                dirChooser = true  // 允许用户选择安装目录
                menuGroup = "诗词收藏"  // 开始菜单分组
                upgradeUuid = "2258659c-c124-4b04-a60e-e5d0f0c5bf48"  // 用于应用更新的唯一标识符

                // 图标配置（可选）
                // iconFile.set(project.file("src/main/resources/icon.ico"))

                // 安装程序配置
                msiPackageVersion = "1.0.0"  // MSI 安装包版本
                exePackageVersion = "1.0.0"  // EXE 安装包版本
            }*/
        }

        buildTypes.release.proguard {
            configurationFiles.from("compose-desktop.pro")
        }
    }
}
