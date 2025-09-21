package com.teamviewer.assistar.demo.detect

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamviewer.assistvision.boot.TfLiteBoot
import com.teamviewer.assistvision.domain.usecase.DetectObjectsUseCase
import com.teamviewer.assistvision.services.nativebridge.NativeObjectRecognitionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

data class UiDetection(
    val label: String,
    val score: Float,
    val left: Float, val top: Float, val right: Float, val bottom: Float
)

data class DetectUiState(
    val fps: Double = 0.0,
    val width: Int = 0, val height: Int = 0,             // rotated image dimensions
    val detections: List<UiDetection> = emptyList(),     // boxes in rotated image-space
    val blurVar: Double = 0.0, val glarePercent: Double = 0.0, val brightness: Double = 0.0,
    val processingMs: Long = 0, val savedShots: List<Uri> = emptyList()
)

class DetectViewModel(
    private val appContext: Context,
    private val nativeSvc: NativeObjectRecognitionService,
    private val detect: DetectObjectsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetectUiState())
    val state = _state.asStateFlow()

    private var jpegBuf = ByteBuffer.allocateDirect(1_000_000)

    fun initializeNative() {
        viewModelScope.launch {
            TfLiteBoot.awaitReady()
            nativeSvc.initializeEmbedded(useXnnpack = true, numThreads = 2)
        }
    }

    fun onFps(fps: Double) { _state.value = _state.value.copy(fps = fps) }

    /**
     * Receives raw YUV420 buffers (sensor orientation), does native detection,
     * then rotates/mirrors bboxes into the *rotated image-space*.
     *
     * @param rotationDeg ImageProxy.imageInfo.rotationDegrees (0/90/180/270)
     * @param isFront If true, mirror horizontally after rotation.
     */
    fun onFrameYuv(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        rotationDeg: Int,
        isFront: Boolean
    ) {
        viewModelScope.launch {
            val r = detect(
                y, u, v, width, height,
                yRowStride, uRowStride, vRowStride, uPixelStride, vPixelStride,
                blurThr = 120.0, glareThrPercent = 8.0, brightnessFloor = 40.0, scoreThr = 0.5f
            )

            // 1) rotate/mirror each detection box from (sensor image-space) to (rotated image-space)
            val (rotW, rotH) = rotatedDims(width, height, rotationDeg)
            val uiDets = r.detections.map { d ->
                // incoming d.left/top/right/bottom are in original (width x height) space
                var bx = Box(d.left, d.top, d.right, d.bottom)
                bx = rotateBox(bx, width.toFloat(), height.toFloat(), rotationDeg)
                if (isFront) bx = mirrorHorizontally(bx, rotW.toFloat())
                UiDetection(
                    label = d.label, score = d.score,
                    left = bx.l, top = bx.t, right = bx.r, bottom = bx.b
                )
            }

            // 2) Demo rule: save if any "banana" >= 0.5
            if (uiDets.any { it.label.equals("banana", true) && it.score >= 0.5f }) {
                saveCurrentJpegNative()
            }

            _state.value = _state.value.copy(
                width = rotW, height = rotH, detections = uiDets,
                blurVar = r.blurVar, glarePercent = r.glarePercent,
                brightness = r.brightness, processingMs = r.processingMs
            )
        }
    }

    private fun saveCurrentJpegNative() {
        var len = nativeSvc.encodeLastJpeg(jpegBuf, 90)
        if (len < 0) {
            val need = -len
            jpegBuf = ByteBuffer.allocateDirect(need)
            len = nativeSvc.encodeLastJpeg(jpegBuf, 90)
        }
        if (len > 0) {
            val bytes = ByteArray(len)
            jpegBuf.rewind(); jpegBuf.get(bytes)
            val file = File(appContext.cacheDir, "shot_${System.currentTimeMillis()}.jpg")
            file.writeBytes(bytes)
            _state.value = _state.value.copy(savedShots = _state.value.savedShots + Uri.fromFile(file))
        }
    }

    private data class Box(var l: Float, var t: Float, var r: Float, var b: Float)

    private fun rotatedDims(w: Int, h: Int, rot: Int): Pair<Int, Int> =
        if (rot % 180 == 0) w to h else h to w

    private fun rotatePoint(x: Float, y: Float, w: Float, h: Float, deg: Int): Pair<Float, Float> = when (deg % 360) {
        90  -> y to (w - x)
        180 -> (w - x) to (h - y)
        270 -> (h - y) to x
        else -> x to y
    }

    private fun rotateBox(b: Box, w: Float, h: Float, deg: Int): Box {
        val c = arrayOf(
            rotatePoint(b.l, b.t, w, h, deg),
            rotatePoint(b.r, b.t, w, h, deg),
            rotatePoint(b.r, b.b, w, h, deg),
            rotatePoint(b.l, b.b, w, h, deg)
        )
        val xs = floatArrayOf(c[0].first, c[1].first, c[2].first, c[3].first)
        val ys = floatArrayOf(c[0].second, c[1].second, c[2].second, c[3].second)
        return Box(xs.min(), ys.min(), xs.max(), ys.max())
    }

    private fun mirrorHorizontally(b: Box, w: Float): Box =
        Box(w - b.r, b.t, w - b.l, b.b)

    private fun FloatArray.min(): Float {
        var m = this[0]; for (i in 1 until size) if (this[i] < m) m = this[i]; return m
    }
    private fun FloatArray.max(): Float {
        var m = this[0]; for (i in 1 until size) if (this[i] > m) m = this[i]; return m
    }
}
