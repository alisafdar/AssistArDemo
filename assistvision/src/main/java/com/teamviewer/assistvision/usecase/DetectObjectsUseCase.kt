package com.teamviewer.assistvision.usecase

import com.teamviewer.assistvision.domain.model.DetectionResult
import com.teamviewer.assistvision.domain.model.DomainDetection
import com.teamviewer.assistvision.domain.usecase.DetectObjectsUseCase
import com.teamviewer.assistvision.services.nativebridge.JNIBridge
import com.teamviewer.assistvision.services.nativebridge.NativeObjectRecognitionService
import java.nio.ByteBuffer
import kotlin.collections.plusAssign

class DetectObjectsUseCaseImpl(
    private val recognitionService: NativeObjectRecognitionService
) : DetectObjectsUseCase {
    override operator fun invoke(
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
    ): DetectionResult {
        val frame = JNIBridge.processFrame(
            y, u, v,
            width, height,
            yRowStride, uRowStride, vRowStride,
            uPixelStride, vPixelStride,
            blur, glarePercent, brightness,
            score,
            rotationDegrees
        )
        val labels = recognitionService.labels
        fun labelFor(id: Int): String = labels.getOrNull(id) ?: labels.getOrNull(id - 1) ?: "id:$id"

        val out = ArrayList<DomainDetection>(frame.scores.size)
        for (i in frame.scores.indices) {
            val left = frame.boxes[i * 4 + 0];
            val top = frame.boxes[i * 4 + 1]
            val right = frame.boxes[i * 4 + 2];
            val bottom = frame.boxes[i * 4 + 3]
            out += DomainDetection(labelFor(frame.classes[i]), frame.scores[i], left, top, right, bottom)
        }
        return DetectionResult(out, frame.blur, frame.glarePercent, frame.brightness, frame.processingMs)
    }
}