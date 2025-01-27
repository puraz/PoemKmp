rootProject.name = "PoemKmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // repositories {
        //     maven {
        //         url = uri("https://maven.aliyun.com/repository/google")
        //     }
        //     maven {
        //         url = uri("https://maven.aliyun.com/repository/jcenter")
        //     }
        //     maven {
        //         url = uri("https://maven.aliyun.com/repository/central")
        //     }
        //     gradlePluginPortal()
        // }
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
    // repositories {
    //     maven {
    //         url = uri("https://maven.aliyun.com/repository/google")
    //     }
    //     maven {
    //         url = uri("https://maven.aliyun.com/repository/jcenter")
    //     }
    //     maven {
    //         url = uri("https://maven.aliyun.com/repository/central")
    //     }
    // }
}

include(":composeApp")