package com.teamviewer.assistvision.ui.overlay

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
import androidx.compose.ui.graphics.nativeCanvas

data class DetectionUi(
    val l: Float, val t: Float, val r: Float, val b: Float,
    val score: Float,
    val classId: Int
)

@Composable
fun DetectionOverlay(
    detections: List<DetectionUi>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        detections.forEach { d ->
            val name = labelFor(d.classId, labels)
            drawBoxWithLabel(d.l, d.t, d.r, d.b, "$name (${(d.score*100).toInt()}%)")
        }
    }
}

private fun labelFor(id: Int, labels: List<String>): String {
    // Many COCO models are 1-based; try exact, then (id-1)
    return labels.getOrNull(id)
        ?: labels.getOrNull(id - 1)
        ?: "id:$id"
}

private fun DrawScope.drawBoxWithLabel(l: Float, t: Float, r: Float, b: Float, text: String) {
    val stroke = Stroke(
        width = 3f,
        pathEffect = PathEffect.cornerPathEffect(8f)
    )
    drawRect(Color.Red, topLeft = Offset(l, t), size = Size(r - l, b - t), style = stroke)

    // Simple text banner
    drawContext.canvas.nativeCanvas.apply {
        val p = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.RED
            textSize = 28f
            typeface = android.graphics.Typeface.create("", android.graphics.Typeface.BOLD)
        }
        drawText(text, l + 6f, t - 10f, p)
    }
}
