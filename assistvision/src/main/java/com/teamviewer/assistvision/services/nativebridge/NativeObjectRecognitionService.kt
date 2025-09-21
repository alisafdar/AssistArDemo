package com.teamviewer.assistvision.services.nativebridge

import android.content.Context
import com.teamviewer.assistvision.domain.model.Detection
import com.teamviewer.assistvision.domain.model.DetectionsResult
import java.nio.ByteBuffer

class NativeObjectRecognitionService(private val appContext: Context) {
    @Volatile private var initialized = false
    /** Public read-only labels resolved from embedded labelmap.txt */
    @Volatile var labels: List<String> = emptyList(); private set

    /**
     * Initialize TFLite with embedded model + labels.
     * GPU delegate is chosen by GMS init on Java side; we only pass XNNPACK + threads.
     */
    fun initializeEmbedded(useXnnpack: Boolean = true, numThreads: Int = 2): Boolean {
        if (initialized) return true
        initialized = JNIBridge.nativeInitEmbeddedSimple(useXnnpack, numThreads)
        if (initialized) {
            labels = JNIBridge.nativeGetLabels().toList()
        }
        return initialized
    }

    /**
     * Preferred API: native rotates the frame by [rotationDeg] (0/90/180/270),
     * runs detection, and returns pixel-space boxes in the rotated image.
     */
    fun processYuv420Rotated(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double,
        scoreThr: Float,
        rotationDeg: Int
    ): DetectionsResult {
        check(initialized) { "Call initializeEmbedded() first" }

        val r = JNIBridge.nativeProcessYuv420Rotated(
            y, u, v,
            width, height,
            yRowStride, uRowStride, vRowStride,
            uPixelStride, vPixelStride,
            blurThr, glareThrPercent, brightnessFloor,
            scoreThr,
            rotationDeg
        )

        val dets = buildList {
            val n = r.scores.size
            for (i in 0 until n) {
                val cls = r.classes[i]
                val label = labelFor(cls)
                add(
                    Detection(
                        classId = cls,
                        label = label,
                        score = r.scores[i],
                        left = r.boxes[i * 4 + 0],
                        top = r.boxes[i * 4 + 1],
                        right = r.boxes[i * 4 + 2],
                        bottom = r.boxes[i * 4 + 3]
                    )
                )
            }
        }
        return DetectionsResult(dets, r.blurVar, r.glarePercent, r.brightness, r.processingMs)
    }

    /** Backward-compat shim: no rotation (calls rotated variant with 0°). */
    @Deprecated("Use processYuv420Rotated(..., rotationDeg) so native rotates before detection")
    fun processYuv420(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double, scoreThr: Float
    ): DetectionsResult =
        processYuv420Rotated(
            y, u, v, width, height,
            yRowStride, uRowStride, vRowStride,
            uPixelStride, vPixelStride,
            blurThr, glareThrPercent, brightnessFloor, scoreThr,
            rotationDeg = 0
        )

    /** JPEG export of the last rotated RGBA frame kept by native. */
    fun encodeLastJpeg(buf: ByteBuffer, quality: Int): Int =
        JNIBridge.nativeEncodeLastJpeg(buf, quality)

    // ---- helpers ----
    private fun labelFor(id: Int): String {
        // Try 0-based first, then 1-based (COCO style). Fallback to "id:x".
        return when {
//            id in labels.indices -> labels[id]
            (id - 1) in labels.indices -> labels[id - 1]
            else -> "id:$id"
        }
    }
}
