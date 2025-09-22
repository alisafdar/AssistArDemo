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
        blur: Double,
        glarePercent: Double,
        brightness: Double,
        score: Float,
        rotationDegrees: Int
    ): DetectionResult
}