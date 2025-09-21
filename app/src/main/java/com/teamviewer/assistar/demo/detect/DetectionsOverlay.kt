package com.teamviewer.assistar.demo.detect

import androidx.compose.ui.graphics.nativeCanvas
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
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Draws detection boxes using center-crop scaling to match PreviewView.
 * @param width  Rotated image width (from VM)
 * @param height Rotated image height (from VM)
 * @param items  Boxes in rotated image pixel space (from VM)
 */
@Composable
fun DetectionsOverlay(
    modifier: Modifier = Modifier,
    width: Int,
    height: Int,
    items: List<UiDetection>
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (width <= 0 || height <= 0) return@Canvas

        // === center-crop from image (width x height) to canvas (size.width x size.height)
        val imgW = width.toFloat()
        val imgH = height.toFloat()
        val canvasW = size.width
        val canvasH = size.height

        val scale = maxOf(canvasW / imgW, canvasH / imgH)
        val dx = (canvasW - imgW * scale) / 2f
        val dy = (canvasH - imgH * scale) / 2f

        withTransform({
            translate(dx, dy)
            scale(scaleX = scale, scaleY = scale)
        }) {
            val stroke = Stroke(
                width = 3f,
                pathEffect = PathEffect.cornerPathEffect(8f)
            )
            items.forEach { d ->
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(d.left, d.top),
                    size = Size(d.right - d.left, d.bottom - d.top),
                    style = stroke
                )
                drawBannerText(d.label, d.score, d.left, d.top)
            }
        }
    }
}

private fun DrawScope.drawBannerText(label: String, score: Float, left: Float, top: Float) {
    val txt = "$label (${(score * 100).toInt()}%)"
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
        val tw = p.measureText(txt)
        val th = p.fontMetrics.let { it.bottom - it.top }
        val bx = left
        val by = (top - th - 6f).coerceAtLeast(4f)
        drawRect(bx - pad, by - pad, bx + tw + pad, by + th - p.fontMetrics.bottom + pad, bg)
        drawText(txt, bx, by - p.fontMetrics.top, p)
    }
}
