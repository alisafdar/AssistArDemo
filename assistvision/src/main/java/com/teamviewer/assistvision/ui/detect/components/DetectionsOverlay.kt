package com.teamviewer.assistvision.ui.detect.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import com.teamviewer.assistvision.ui.detect.UiDetection
import kotlin.math.max

@Composable
fun DetectionsOverlay(
    modifier: Modifier = Modifier, width: Int, height: Int, items: List<UiDetection>
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (width <= 0 || height <= 0 || items.isEmpty()) return@Canvas

        val imageWidth = width.toFloat()
        val imageHeight = height.toFloat()
        val canvasWidth = size.width
        val canvasHeight = size.height

        val scale = max(canvasWidth / imageWidth, canvasHeight / imageHeight)
        val dx = (canvasWidth - imageWidth * scale) / 2f
        val dy = (canvasHeight - imageHeight * scale) / 2f

        withTransform({
            translate(dx, dy)
            scale(scaleX = scale, scaleY = scale)
        }) {
            clipRect(left = 0f, top = 0f, right = imageWidth, bottom = imageHeight) {
                val stroke = Stroke(width = 3f, pathEffect = PathEffect.cornerPathEffect(8f))
                items.forEach { boundingBox ->
                    val leftCoordinate = boundingBox.left.coerceIn(0f, imageWidth)
                    val topCoordinate = boundingBox.top.coerceIn(0f, imageHeight)
                    val rightCoordinate = boundingBox.right.coerceIn(0f, imageWidth)
                    val bottomCoordinate = boundingBox.bottom.coerceIn(0f, imageHeight)
                    if (rightCoordinate > leftCoordinate && bottomCoordinate > topCoordinate) {
                        drawRect(Color.Red, topLeft = Offset(leftCoordinate, topCoordinate), size = Size(rightCoordinate - leftCoordinate, bottomCoordinate - topCoordinate), style = stroke)
                        drawBanner("${boundingBox.label} (${(boundingBox.score * 100).toInt()}%)", leftCoordinate, topCoordinate)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBanner(text: String, left: Float, top: Float) {
    drawContext.canvas.nativeCanvas.apply {
        val backgroundPaint = Paint().apply {
            color = 0x99000000.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val paint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.RED
            textSize = 24f
            typeface = Typeface.create("", Typeface.BOLD)
        }
        val padding = 6f
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val textHeight = fontMetrics.bottom - fontMetrics.top
        val bx = left
        val by = (top - textHeight - 6f).coerceAtLeast(4f)
        drawRect(bx - padding, by - padding, bx + textWidth + padding, by + textHeight - fontMetrics.bottom + padding, backgroundPaint)
        drawText(text, bx, by - fontMetrics.top, paint)
    }
}
