package com.teamviewer.assistvision.ui.overlay

import android.graphics.Matrix
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

data class PxBox(var l: Float, var t: Float, var r: Float, var b: Float) {
    fun toRectF() = RectF(l, t, r, b)
}

private fun rotatePoint(x: Float, y: Float, w: Float, h: Float, deg: Int): Pair<Float, Float> = when (deg % 360) {
    90  -> y to (w - x)
    180 -> (w - x) to (h - y)
    270 -> (h - y) to x
    else -> x to y
}

/** Rotate a box around origin for a source image of size (w,h) by rotationDegrees. */
fun rotateBoxPx(src: PxBox, w: Int, h: Int, rotationDegrees: Int): PxBox {
    val corners = arrayOf(
        rotatePoint(src.l, src.t, w.toFloat(), h.toFloat(), rotationDegrees),
        rotatePoint(src.r, src.t, w.toFloat(), h.toFloat(), rotationDegrees),
        rotatePoint(src.r, src.b, w.toFloat(), h.toFloat(), rotationDegrees),
        rotatePoint(src.l, src.b, w.toFloat(), h.toFloat(), rotationDegrees)
    )
    val xs = corners.map { it.first }
    val ys = corners.map { it.second }
    return PxBox(xs.min(), ys.min(), xs.max(), ys.max())
}

/** Optional mirror for front camera after rotation. */
fun mirrorHorizontallyPx(src: PxBox, w: Int): PxBox =
    PxBox(w - src.r, src.t, w - src.l, src.b)

/** Map image px → view px with center-crop (PreviewView default). */
fun centerCropScaleToViewPx(src: PxBox, imgW: Int, imgH: Int, viewW: Float, viewH: Float): PxBox {
    val scale = max(viewW / imgW, viewH / imgH)
    val dx = (viewW - imgW * scale) / 2f
    val dy = (viewH - imgH * scale) / 2f
    return PxBox(
        l = src.l * scale + dx,
        t = src.t * scale + dy,
        r = src.r * scale + dx,
        b = src.b * scale + dy
    )
}
