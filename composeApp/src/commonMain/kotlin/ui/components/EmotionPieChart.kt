package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import service.EmotionWithIntensity


// Annotation class for keyword highlighting
class KeywordAnnotation

@Composable
fun TextView(text: AnnotatedString, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
    )
}

@Composable
fun PieChart(emotions: List<EmotionWithIntensity>, modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFF6200EE),
        Color(0xFF3700B3),
        Color(0xFF03DAC5)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 左对齐的标题
        Text(
            text = "情感分布",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 让图表和标注整体居中
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically // 让图表和标注垂直居中对齐
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val total = emotions.sumOf { it.intensity.toDouble() }.toFloat()
                        var startAngle = 0f

                        emotions.forEachIndexed { index, emotion ->
                            val sweepAngle = 360f * (emotion.intensity / total)
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = Size(size.width, size.height),
                                topLeft = Offset.Zero
                            )
                            startAngle += sweepAngle
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp)) // 增加间距，让图表和标注有一定距离
                Column(
                    modifier = Modifier.wrapContentWidth()
                ) {
                    emotions.forEachIndexed { index, emotion ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colors[index % colors.size])
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${emotion.emotion} (${(emotion.intensity * 100).toInt()}%)")
                        }
                    }
                }
            }
        }
    }
}


// Extension function for AnnotatedString.Builder
fun AnnotatedString.Builder.findOffset(substring: String): Int? {
    val text = this.toString()
    return text.indexOf(substring).takeIf { it >= 0 }
}
