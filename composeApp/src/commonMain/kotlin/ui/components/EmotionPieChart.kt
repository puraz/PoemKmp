package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

    Column(modifier = modifier.padding(16.dp)) {
        Text("情感分布")
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
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

            // Legend
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                emotions.forEachIndexed { index, emotion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(2.dp)
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

// Extension function for AnnotatedString.Builder
fun AnnotatedString.Builder.findOffset(substring: String): Int? {
    val text = this.toString()
    return text.indexOf(substring).takeIf { it >= 0 }
}