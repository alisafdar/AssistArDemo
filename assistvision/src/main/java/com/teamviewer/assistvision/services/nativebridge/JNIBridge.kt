package com.teamviewer.assistvision.services.nativebridge

import java.nio.ByteBuffer

internal object JNIBridge {
    init { System.loadLibrary("assistvision") }

    external fun nativeInitEmbeddedSimple(useXnnpack: Boolean, numThreads: Int): Boolean

    external fun nativeProcessYuv420(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThreshold: Double, glareThresholdPercent: Double, brightnessFloor: Double,
        scoreThreshold: Float
    ): NativeDetections

    external fun nativeEncodeLastJpeg(out: ByteBuffer, capacity: Int, quality: Int): Int

    external fun nativeGetLabels(): Array<String>

    data class NativeDetections(
        val boxes: FloatArray, val scores: FloatArray, val classes: IntArray,
        val blurVar: Double, val glarePercent: Double, val brightness: Double, val processingMs: Long
    )
}
