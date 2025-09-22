package com.teamviewer.assistvision.domain.service

import com.teamviewer.assistvision.domain.model.DetectionsResult
import java.nio.ByteBuffer

interface ObjectRecognitionService {
    fun initializeEmbedded(useXnnpack: Boolean = true, numThreads: Int = 2): Boolean
    fun processYuv420Rotated(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double,
        scoreThr: Float,
        rotationDeg: Int
    ): DetectionsResult

    fun encodeLastJpeg(buf: ByteBuffer, quality: Int): Int
}