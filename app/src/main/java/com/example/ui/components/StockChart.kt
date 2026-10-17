package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoricalPoint
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun InteractiveStockChart(
    points: List<HistoricalPoint>,
    modifier: Modifier = Modifier,
    isPositive: Boolean = true,
    onScrub: (HistoricalPoint?) -> Unit = {}
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Loading real-time price history...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        return
    }

    var touchX by remember { mutableStateOf<Float?>(null) }
    val lineColor = if (isPositive) GainGreen else LossRed
    val gradientStart = if (isPositive) Color(0x3300E676) else Color(0x33FF5252)
    val gradientEnd = Color(0x00000000)

    val minPrice = points.minOf { it.low }
    val maxPrice = points.maxOf { it.high }
    val priceRange = if (maxPrice > minPrice) (maxPrice - minPrice) else 1.0

    val maxVolume = (points.maxOfOrNull { it.volume } ?: 1000L).coerceAtLeast(100L)

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .pointerInput(points) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchX = offset.x
                            val idx = ((offset.x / size.width) * (points.size - 1))
                                .toInt()
                                .coerceIn(0, points.size - 1)
                            onScrub(points[idx])
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            touchX = change.position.x
                            val idx = ((change.position.x / size.width) * (points.size - 1))
                                .toInt()
                                .coerceIn(0, points.size - 1)
                            onScrub(points[idx])
                        },
                        onDragEnd = {
                            touchX = null
                            onScrub(null)
                        },
                        onDragCancel = {
                            touchX = null
                            onScrub(null)
                        }
                    )
                }
                .pointerInput(points) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchX = offset.x
                            val idx = ((offset.x / size.width) * (points.size - 1))
                                .toInt()
                                .coerceIn(0, points.size - 1)
                            onScrub(points[idx])
                            tryAwaitRelease()
                            touchX = null
                            onScrub(null)
                        }
                    )
                }
        ) {
            val width = size.width
            val totalHeight = size.height
            val chartHeight = totalHeight * 0.78f
            val volumeHeight = totalHeight * 0.20f
            val volumeTop = totalHeight * 0.80f

            // Draw horizontal dashed grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = (chartHeight / gridLines) * i
                drawLine(
                    color = SurfaceBorder,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw Volume Bars
            val barWidth = (width / points.size).coerceAtLeast(2.dp.toPx())
            for (i in points.indices) {
                val p = points[i]
                val x = (i.toFloat() / (points.size - 1).coerceAtLeast(1)) * width
                val volFraction = (p.volume.toFloat() / maxVolume).coerceIn(0.05f, 1f)
                val barH = volumeHeight * volFraction
                val barY = totalHeight - barH
                val isUp = p.close >= p.open
                drawRect(
                    color = if (isUp) Color(0x3300E676) else Color(0x33FF5252),
                    topLeft = Offset(x - (barWidth / 2), barY),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, barH)
                )
            }

            // Build line & fill path
            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { index, point ->
                val x = (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * width
                val normY = (point.price - minPrice) / priceRange
                val y = chartHeight - (normY.toFloat() * chartHeight * 0.90f) - (chartHeight * 0.05f)

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, chartHeight)
                    fillPath.lineTo(x, y)
                } else {
                    val prevIdx = index - 1
                    val prevX = (prevIdx.toFloat() / (points.size - 1).coerceAtLeast(1)) * width
                    val prevNormY = (points[prevIdx].price - minPrice) / priceRange
                    val prevY = chartHeight - (prevNormY.toFloat() * chartHeight * 0.90f) - (chartHeight * 0.05f)

                    val cx = (prevX + x) / 2
                    path.cubicTo(cx, prevY, cx, y, x, y)
                    fillPath.cubicTo(cx, prevY, cx, y, x, y)
                }
            }

            // Complete fill path
            fillPath.lineTo(width, chartHeight)
            fillPath.close()

            // Draw Area Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(gradientStart, gradientEnd),
                    startY = 0f,
                    endY = chartHeight
                )
            )

            // Draw Price Stroke
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Handle Active Scrub Crosshair & Indicator Dot
            touchX?.let { currentTouchX ->
                val clampedX = currentTouchX.coerceIn(0f, width)
                val activeIdx = ((clampedX / width) * (points.size - 1))
                    .toInt()
                    .coerceIn(0, points.size - 1)
                val point = points[activeIdx]
                val pointX = (activeIdx.toFloat() / (points.size - 1).coerceAtLeast(1)) * width
                val normY = (point.price - minPrice) / priceRange
                val pointY = chartHeight - (normY.toFloat() * chartHeight * 0.90f) - (chartHeight * 0.05f)

                // Vertical scrub line
                drawLine(
                    color = BrandTealGlow,
                    start = Offset(pointX, 0f),
                    end = Offset(pointX, totalHeight),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Outer halo
                drawCircle(
                    color = Color(0x6606B6D4),
                    radius = 9.dp.toPx(),
                    center = Offset(pointX, pointY)
                )
                // Center point
                drawCircle(
                    color = BrandTealGlow,
                    radius = 4.5.dp.toPx(),
                    center = Offset(pointX, pointY)
                )
            }
        }
    }
}

@Composable
fun Sparkline(
    values: List<Double>,
    modifier: Modifier = Modifier,
    isPositive: Boolean = true
) {
    if (values.size < 2) return

    val strokeColor = if (isPositive) GainGreen else LossRed
    val minVal = values.minOrNull() ?: 0.0
    val maxVal = values.maxOrNull() ?: 1.0
    val range = if (maxVal > minVal) (maxVal - minVal) else 1.0

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path()

        values.forEachIndexed { i, v ->
            val x = (i.toFloat() / (values.size - 1)) * w
            val normY = ((v - minVal) / range).toFloat()
            val y = h - (normY * h * 0.85f) - (h * 0.075f)

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
