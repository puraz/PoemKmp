data class Poetry(
    val id: Long = 0,
    val title: String,
    val content: String,
    val author: String,
    val dynasty: String? = null,
    val tags: List<String> = emptyList(),
    val category: Category,
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val appreciation: String? = null  // 添加鉴赏字段
)

enum class Category {
    ANCIENT_POETRY,   // 古诗
    MODERN_POETRY,    // 现代诗
    QUOTE,           // 名言
    OTHER            // 其他
} 