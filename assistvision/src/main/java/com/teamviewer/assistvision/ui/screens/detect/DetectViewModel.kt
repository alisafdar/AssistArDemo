package com.teamviewer.assistvision.ui.screens.detect

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamviewer.assistar.demo.utils.ResourceProvider
import com.teamviewer.assistvision.services.boot.TfLiteBoot
import com.teamviewer.assistvision.usecase.DetectObjectsUseCaseImpl
import com.teamviewer.assistvision.services.nativebridge.NativeObjectRecognitionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

class DetectViewModel(
    private val resourceProvider: ResourceProvider,
    private val recognitionService: NativeObjectRecognitionService,
    private val detectObjectUseCase: DetectObjectsUseCaseImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetectUiState())
    val uiState = _uiState.asStateFlow()
    private var jpegOutputBuffer = ByteBuffer.allocateDirect(1_000_000)

    fun initializeNative() {
        viewModelScope.launch(Dispatchers.Default) {
            TfLiteBoot.awaitReady()
            recognitionService.initialize()
        }
    }

    fun onFps(fps: Double) {
        _uiState.value = _uiState.value.copy(fps = fps)
    }

    fun onFrame(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer, width: Int, height: Int, yRowStride: Int, uRowStride: Int, vRowStride: Int, uPixelStride: Int, vPixelStride: Int, rotationDegrees: Int
    ) {
        val result = detectObjectUseCase(
            y,
            u,
            v,
            width,
            height,
            yRowStride,
            uRowStride,
            vRowStride,
            uPixelStride,
            vPixelStride,
            blur = 120.0,
            glarePercent = 8.0,
            brightness = 40.0,
            score = 0.5f,
            rotationDegrees = rotationDegrees
        )

        val (rotatedWidth, rotatedHeight) = rotatedDimens(width, height, rotationDegrees)

        val processedDetections = buildList {
            for (detection in result.detections) {
                val box = clamp(
                    Box(
                        left = detection.left,
                        top = detection.top,
                        right = detection.right,
                        bottom = detection.bottom
                    ),
                    width = rotatedWidth.toFloat(),
                    height = rotatedHeight.toFloat()
                )
                val widthPixels = box.right - box.left
                val heightPixels = box.bottom - box.top
                if (widthPixels >= 2f && heightPixels >= 2f) {
                    add(
                        UiDetection(
                            label = detection.label,
                            score = detection.score,
                            left =  box.left,
                            top = box.top,
                            right = box.right,
                            bottom = box.bottom
                        )
                    )
                }
            }
        }

        if (processedDetections.any { it.label.equals("banana", true) && it.score >= 0.5f }) {
            viewModelScope.launch(Dispatchers.IO) {
                saveCurrentJpegNative()
            }
        }

        viewModelScope.launch(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(
                width = rotatedWidth, height = rotatedHeight, detections = processedDetections, blurVar = result.blur, glarePercent = result.glarePercent, brightness = result.brightness, processingMs = result.processingDuration
            )
        }
    }

    private fun saveCurrentJpegNative() {
        var encodedLength = recognitionService.encodeLastFrame(jpegOutputBuffer, 90)
        if (encodedLength < 0) {
            val need = -encodedLength
            jpegOutputBuffer = ByteBuffer.allocateDirect(need)
            encodedLength = recognitionService.encodeLastFrame(jpegOutputBuffer, 90)
        }
        if (encodedLength > 0) {
            val bytes = ByteArray(encodedLength)
            jpegOutputBuffer.rewind(); jpegOutputBuffer.get(bytes)
            val file = File(resourceProvider.getCacheDirectory(), "shot_${System.currentTimeMillis()}.jpg")
            file.writeBytes(bytes)
            viewModelScope.launch(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(savedShots = _uiState.value.savedShots + Uri.fromFile(file))
            }
        }
    }

    private data class Box(var left: Float, var top: Float, var right: Float, var bottom: Float)
    private fun rotatedDimens(width: Int, height: Int, rotationAngle: Int): Pair<Int, Int> = if (rotationAngle % 180 == 0) width to height else height to width
    private fun clamp(box: Box, width: Float, height: Float): Box = Box(
        left = box.left.coerceIn(0f, width), top = box.top.coerceIn(0f, height), right = box.right.coerceIn(0f, width), bottom = box.bottom.coerceIn(0f, height)
    )
}
