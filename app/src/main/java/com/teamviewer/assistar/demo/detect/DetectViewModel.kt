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

data class UiDetection(val label:String, val score:Float,
                       val left:Float, val top:Float, val right:Float, val bottom:Float)

data class DetectUiState(
    val fps: Double = 0.0,
    val width: Int = 0, val height: Int = 0,                // ROTATED image dims
    val detections: List<UiDetection> = emptyList(),         // pixel-space in rotated image
    val blurVar: Double = 0.0, val glarePercent: Double = 0.0, val brightness: Double = 0.0,
    val processingMs: Long = 0, val savedShots: List<Uri> = emptyList()
)

class DetectViewModel(
    private val appContext: Context,
    private val nativeSvc: NativeObjectRecognitionService,
    private val detect: DetectObjectsUseCase
): ViewModel() {

    private val _state = MutableStateFlow(DetectUiState())
    val state = _state.asStateFlow()
    private var jpegBuf = ByteBuffer.allocateDirect(1_000_000)

    fun initializeNative() {
        viewModelScope.launch {
            TfLiteBoot.awaitReady()
            if (nativeSvc.initializeEmbedded(useXnnpack = true, numThreads = 2)) {
                // (optional) you could expose labels in UI state if using the library overlay
            }
        }
    }

    fun onFps(fps: Double) { _state.value = _state.value.copy(fps=fps) }

    fun onFrameYuv(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int, uPixelStride: Int, vPixelStride: Int,
        rotationDeg: Int,
        isFront: Boolean // Ignored; our native doesn’t mirror. (Preview isn’t mirrored by default)
    ) {
        viewModelScope.launch {
            val r = detect(
                y,u,v,width,height,
                yRowStride,uRowStride,vRowStride,uPixelStride,vPixelStride,
                blurThr=120.0, glareThrPercent=8.0, brightnessFloor=40.0, scoreThr=0.5f,
                rotationDeg = rotationDeg
            )

            val uiDets = r.detections.map { UiDetection(it.label,it.score,it.left,it.top,it.right,it.bottom) }

            if (uiDets.any { it.label.equals("banana", true) && it.score >= 0.5f }) {
                saveCurrentJpegNative()
            }

            val (rotW, rotH) = if (rotationDeg % 180 == 0) width to height else height to width

            _state.value = _state.value.copy(
                width=rotW, height=rotH, detections=uiDets,
                blurVar=r.blurVar, glarePercent=r.glarePercent, brightness=r.brightness, processingMs=r.processingMs
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
}
