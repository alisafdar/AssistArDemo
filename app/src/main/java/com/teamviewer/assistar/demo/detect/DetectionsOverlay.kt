package com.teamviewer.assistar.demo.detect

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas

@Composable
fun DetectionsOverlay(
    modifier: Modifier = Modifier,
    width: Int,
    height: Int,
    items: List<UiDetection>
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (width <= 0 || height <= 0 || items.isEmpty()) return@Canvas

        val imgW = width.toFloat()
        val imgH = height.toFloat()
        val canvasW = size.width
        val canvasH = size.height

        // center-crop scale + offset (same as PreviewView)
        val scale = kotlin.math.max(canvasW / imgW, canvasH / imgH)
        val dx = (canvasW - imgW * scale) / 2f
        val dy = (canvasH - imgH * scale) / 2f

        withTransform({
            translate(dx, dy)
            scale(scaleX = scale, scaleY = scale)
        }) {
            // Draw ONLY within the image rect after transform
            clipRect(left = 0f, top = 0f, right = imgW, bottom = imgH) {
                val stroke = Stroke(width = 3f, pathEffect = PathEffect.cornerPathEffect(8f))
                items.forEach { d ->
                    val l = d.left.coerceIn(0f, imgW)
                    val t = d.top.coerceIn(0f, imgH)
                    val r = d.right.coerceIn(0f, imgW)
                    val b = d.bottom.coerceIn(0f, imgH)
                    if (r > l && b > t) {
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(l, t),
                            size = Size(r - l, b - t),
                            style = stroke
                        )
                        drawBanner("${d.label} (${(d.score * 100).toInt()}%)", l, t)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBanner(text: String, l: Float, t: Float) {
    drawContext.canvas.nativeCanvas.apply {
        val bg = android.graphics.Paint().apply {
            color = 0x99_00_00_00.toInt()
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }
        val p = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.RED
            textSize = 24f
            typeface = android.graphics.Typeface.create("", android.graphics.Typeface.BOLD)
        }
        val pad = 6f
        val tw = p.measureText(text)
        val fm = p.fontMetrics
        val th = fm.bottom - fm.top
        val bx = l
        val by = (t - th - 6f).coerceAtLeast(4f)
        drawRect(bx - pad, by - pad, bx + tw + pad, by + th - fm.bottom + pad, bg)
        drawText(text, bx, by - fm.top, p)
    }
}
