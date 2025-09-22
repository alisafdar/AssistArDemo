package com.teamviewer.assistvision.services.nativebridge

import com.teamviewer.assistvision.domain.model.Detection
import com.teamviewer.assistvision.domain.model.DetectionsResult
import com.teamviewer.assistvision.domain.service.ObjectRecognitionService
import java.nio.ByteBuffer

class NativeObjectRecognitionService: ObjectRecognitionService {
    @Volatile private var initialized = false
    @Volatile var labels: List<String> = emptyList(); private set

    override fun initialize(): Boolean {
        if (initialized) return true
        initialized = JNIBridge.initialize()
        if (initialized) {
            labels = JNIBridge.getLabels().toList()
        }
        return initialized
    }

    /**
     * Preferred API: native rotates the frame by [rotationDeg] (0/90/180/270),
     * runs detection, and returns pixel-space boxes in the rotated image.
     */
    override fun processFrame(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double,
        scoreThr: Float,
        rotationDeg: Int
    ): DetectionsResult {
        check(initialized) { "Call initializeEmbedded() first" }

        val frame = JNIBridge.processFrame(
            y, u, v,
            width, height,
            yRowStride, uRowStride, vRowStride,
            uPixelStride, vPixelStride,
            blurThr, glareThrPercent, brightnessFloor,
            scoreThr,
            rotationDeg
        )

        val detections = buildList {
            val n = frame.scores.size
            for (i in 0 until n) {
                val cls = frame.classes[i]
                val label = labelFor(cls)
                add(
                    Detection(
                        classId = cls,
                        label = label,
                        score = frame.scores[i],
                        left = frame.boxes[i * 4 + 0],
                        top = frame.boxes[i * 4 + 1],
                        right = frame.boxes[i * 4 + 2],
                        bottom = frame.boxes[i * 4 + 3]
                    )
                )
            }
        }
        return DetectionsResult(detections, frame.blur, frame.glarePercent, frame.brightness, frame.processingMs)
    }

    override fun encodeLastFrame(buffer: ByteBuffer, quality: Int): Int =
        JNIBridge.encodeFrame(buffer, quality)

    private fun labelFor(id: Int): String {
        // Try 0-based first, then 1-based (COCO style). Fallback to "id:x".
        return when {
            id in labels.indices -> labels[id]
            (id - 1) in labels.indices -> labels[id - 1]
            else -> "id:$id"
        }
    }
}
