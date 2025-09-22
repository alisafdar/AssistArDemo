package com.teamviewer.assistvision.domain.usecase

import com.teamviewer.assistvision.domain.model.DetectionResult
import java.nio.ByteBuffer

interface DetectObjectsUseCase {
    operator fun invoke(
        y: ByteBuffer,
        u: ByteBuffer,
        v: ByteBuffer,
        width: Int,
        height: Int,
        yRowStride: Int,
        uRowStride: Int,
        vRowStride: Int,
        uPixelStride: Int,
        vPixelStride: Int,
        blurThr: Double,
        glareThrPercent: Double,
        brightnessFloor: Double,
        scoreThr: Float,
        rotationDeg: Int
    ): DetectionResult
}