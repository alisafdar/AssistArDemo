package com.teamviewer.assistvision.ui.detect

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamviewer.assistvision.boot.TfLiteBoot
import com.teamviewer.assistvision.usecase.DetectObjectsUseCaseImpl
import com.teamviewer.assistvision.services.nativebridge.NativeObjectRecognitionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

data class UiDetection(
    val label: String, val score: Float,
    val left: Float, val top: Float, val right: Float, val bottom: Float
)

data class DetectUiState(
    val fps: Double = 0.0,
    val width: Int = 0, val height: Int = 0,
    val detections: List<UiDetection> = emptyList(),
    val blurVar: Double = 0.0, val glarePercent: Double = 0.0, val brightness: Double = 0.0,
    val processingMs: Long = 0, val savedShots: List<Uri> = emptyList()
)

class DetectViewModel(
    private val appContext: Context,
    private val nativeSvc: NativeObjectRecognitionService,
    private val detect: DetectObjectsUseCaseImpl
) : ViewModel() {

    private val _state = MutableStateFlow(DetectUiState())
    val state = _state.asStateFlow()

    private var jpegBuf = ByteBuffer.allocateDirect(1_000_000)

    fun initializeNative() {
        viewModelScope.launch(Dispatchers.Default) {
            TfLiteBoot.awaitReady()
            nativeSvc.initializeEmbedded(useXnnpack = true, numThreads = 2)
        }
    }

    fun onFps(fps: Double) { _state.value = _state.value.copy(fps = fps) }

    /**
     * IMPORTANT: This runs on the CameraX analyzer thread and is synchronous so the ImageProxy
     * stays open while native reads the Y/U/V buffers. Do NOT launch a coroutine here.
     */
    fun onFrameYuv(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        rotationDeg: Int, isFront: Boolean
    ) {
        // Native pipeline rotates internally; we only need rotated dimensions for overlay scaling.
        val result = detect(
            y, u, v, width, height,
            yRowStride, uRowStride, vRowStride, uPixelStride, vPixelStride,
            blurThr = 120.0, glareThrPercent = 8.0, brightnessFloor = 40.0, scoreThr = 0.5f,
            rotationDeg = rotationDeg
        )

        val (rotW, rotH) = rotatedDims(width, height, rotationDeg)

        // Boxes are already in rotated image pixel space (native rotates). Clamp to bounds.
        val uiDets = buildList {
            for (d in result.detections) {
                val b = clamp(Box(d.left, d.top, d.right, d.bottom), rotW.toFloat(), rotH.toFloat())
                val wpx = b.r - b.l
                val hpx = b.b - b.t
                if (wpx >= 2f && hpx >= 2f) {
                    add(UiDetection(d.label, d.score, b.l, b.t, b.r, b.b))
                }
            }
        }

        if (uiDets.any { it.label.equals("banana", true) && it.score >= 0.5f }) {
            // Do JPEG work off-thread; do NOT block analyzer
            viewModelScope.launch(Dispatchers.IO) { saveCurrentJpegNative() }
        }

        // Publish state on main
        viewModelScope.launch(Dispatchers.Main) {
            _state.value = _state.value.copy(
                width = rotW, height = rotH, detections = uiDets,
                blurVar = result.blur, glarePercent = result.glarePercent,
                brightness = result.brightness, processingMs = result.processingDuration
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
            viewModelScope.launch(Dispatchers.Main) {
                _state.value = _state.value.copy(savedShots = _state.value.savedShots + Uri.fromFile(file))
            }
        }
    }

    private data class Box(var l: Float, var t: Float, var r: Float, var b: Float)

    private fun rotatedDims(w: Int, h: Int, rot: Int): Pair<Int, Int> =
        if (rot % 180 == 0) w to h else h to w

    private fun clamp(b: Box, w: Float, h: Float): Box =
        Box(
            l = b.l.coerceIn(0f, w),
            t = b.t.coerceIn(0f, h),
            r = b.r.coerceIn(0f, w),
            b = b.b.coerceIn(0f, h)
        )
}
