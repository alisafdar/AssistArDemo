package com.teamviewer.assistvision.domain.service

import com.teamviewer.assistvision.domain.model.DetectionsResult
import java.nio.ByteBuffer

interface ObjectRecognitionService {
    fun initialize(): Boolean
    fun processFrame(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double,
        scoreThr: Float,
        rotationDeg: Int
    ): DetectionsResult

    fun encodeLastFrame(buffer: ByteBuffer, quality: Int): Int
}