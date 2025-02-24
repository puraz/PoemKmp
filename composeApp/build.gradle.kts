import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.1.0" // Use your Kotlin version
    id("app.cash.sqldelight") version "2.0.1"
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    
    sourceSets {

        val commonMain by getting {
            dependencies {
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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
        val desktopMain by getting {
            resources.srcDirs("src/desktopMain/resources")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            }
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
        // 添加编码参数
        jvmArgs += listOf("-Dfile.encoding=UTF-8")

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                // TargetFormat.AppImage,  // Remove AppImage for macOS
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm
            )
            // 从环境变量或 gradle.properties 获取版本号
            packageVersion = System.getenv("VERSION")?.removePrefix("v") ?: "1.0.0"

            // RPM 包信息配置
            linux {
                packageName = "poemkmp"
                // LingXi Verse
                debMaintainer = "puraz2258@gmail.com"
                appCategory = "Utility"
                menuGroup = "Utility"
                installationPath = "/opt/\${packageName}"
                rpmLicenseType = "Apache 2.0"
                // 使用与 packageVersion 相同的版本号
                rpmPackageVersion = packageVersion
            }

            windows {
                packageName = "灵犀诗境"
                dirChooser = true
                menuGroup = "灵犀诗境"
                upgradeUuid = "2258659c-c124-4b04-a60e-e5d0f0c5bf48"
                // 使用与 packageVersion 相同的版本号
                msiPackageVersion = packageVersion
                exePackageVersion = packageVersion
            }

            macOS {
                packageName = "灵犀诗境"
                // 添加 macOS 特定的签名配置（如果需要）
                signing {
                    sign.set(false) // 如果没有证书，设置为 false
                }
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from("compose-desktop.pro")
        }

        // 添加字体资源
        // jvmArgs += listOf(
        //     "-Dfile.encoding=UTF-8",
        //     "-Dprism.fontdir=resources/font"
        // )
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
