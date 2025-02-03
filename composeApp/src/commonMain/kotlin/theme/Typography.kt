import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // 标题: 优先使用黑体
    h5 = TextStyle(
        fontFamily = FontFamily.SansSerif,  // 使用系统无衬线字体
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 1.4.em,
        letterSpacing = 0.5.sp
    ),

    // 副标题
    h6 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 1.4.em
    ),

    // 正文: 优先使用宋体
    body1 = TextStyle(
        fontFamily = FontFamily.Serif,  // 使用系统衬线字体
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 1.7.em,
        letterSpacing = 0.3.sp
    ),

    // 注释、说明文字
    body2 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 1.6.em,
        letterSpacing = 0.2.sp
    ),

    // 小标题
    subtitle1 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 1.5.em,
        letterSpacing = 0.15.sp
    ),

    // 辅助文字
    caption = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 1.4.em,
        letterSpacing = 0.1.sp
    )
) 