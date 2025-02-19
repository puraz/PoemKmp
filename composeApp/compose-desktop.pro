# 保留 Kotlin 相关类
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class org.jetbrains.** { *; }

# 保留 Compose 相关类
-keep class androidx.compose.** { *; }

# 保留 SQLite 相关类
-keep class org.sqlite.** { *; }
-keep class org.sqlite.core.** { *; }
-keep class org.sqlite.jdbc.** { *; }
-keep class org.sqlite.jdbc4.** { *; }
-keep class app.cash.sqldelight.** { *; }

# 保留 JDBC 相关类
-keep class java.sql.** { *; }
-keep class javax.sql.** { *; }

# 不混淆数据库驱动
-keepnames class org.sqlite.JDBC
-keep class org.sqlite.JDBC { *; }

# 忽略 slf4j 相关警告
-dontwarn org.slf4j.**
-dontwarn org.slf4j.impl.**

# 忽略 ktor 相关警告
-dontwarn io.ktor.**

# 保留主程序入口
-keep class MainKt {
    public static void main(java.lang.String[]);
}

# 忽略一些通用警告
-dontwarn sun.font.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn org.w3c.**

# 保留 Manifest 文件
-keep class META-INF.** { *; }

# 不混淆序列化相关的类
-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}

# 保留反射相关
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes InnerClasses 