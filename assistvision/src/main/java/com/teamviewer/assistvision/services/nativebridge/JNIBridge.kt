package com.teamviewer.assistvision.services.nativebridge

import java.nio.ByteBuffer

/**
 * JNI entry points for AssistVision native pipeline.
 *
 * NOTE: Do NOT load the .so here. Load once in App.onCreate() via:
 *   TfLiteBoot.initBlocking(...); TfLiteBoot.ensureNativeLibraryLoaded("assistvision")
 */
object JNIBridge {

    /** Initialize native with embedded TFLite model + labels. GPU is chosen by GMS init on Java side. */
    @JvmStatic
    external fun nativeInitEmbeddedSimple(useXnnpack: Boolean, numThreads: Int): Boolean

    /** Labels resolved from embedded labelmap.txt. */
    @JvmStatic
    external fun nativeGetLabels(): Array<String>

    /**
     * Rotation-aware processing (preferred).
     * Native will:
     *  - pack 3-plane YUV420 to NV21/I420 internally
     *  - convert to RGBA
     *  - rotate by [rotationDeg] (0/90/180/270)
     *  - run inference on the rotated frame
     *  - return pixel-space boxes in that rotated image space
     */
    @JvmStatic
    external fun nativeProcessYuv420Rotated(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double,
        scoreThr: Float,
        rotationDeg: Int
    ): NativeDetections

    /** JPEG-encode the last rotated RGBA frame kept by native into [outBuffer] (capacity provided). Returns byte length or negative needed size. */
    @JvmStatic
    external fun nativeEncodeLastJpeg(outBuffer: ByteBuffer, capacity: Int, quality: Int): Int

    /**
     * Result container mirrored by the native code.
     * Constructor signature used in JNI: ([F [F [I D D D J) V
     */
    data class NativeDetections(
        val boxes: FloatArray,    // [left, top, right, bottom] * N (pixels in rotated image space)
        val scores: FloatArray,   // N
        val classes: IntArray,    // N (raw class ids)
        val blurVar: Double,
        val glarePercent: Double,
        val brightness: Double,
        val processingMs: Long
    )
}
