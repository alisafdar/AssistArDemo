package com.teamviewer.assistvision.services.nativebridge

import java.nio.ByteBuffer

object JNIBridge {
    @JvmStatic
    external fun nativeInitEmbeddedSimple(useXnnpack: Boolean, numThreads: Int): Boolean

    @JvmStatic
    external fun nativeGetLabels(): Array<String>

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

    @JvmStatic
    external fun nativeEncodeLastJpeg(buffer: ByteBuffer, quality: Int): Int

    data class NativeDetections(
        val boxes: FloatArray,    // [left, top, right, bottom] * N (pixels in rotated image space)
        val scores: FloatArray,   // N
        val classes: IntArray,    // N (raw class ids)
        val blurVar: Double,
        val glarePercent: Double,
        val brightness: Double,
        val processingMs: Long
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NativeDetections

            if (blurVar != other.blurVar) return false
            if (glarePercent != other.glarePercent) return false
            if (brightness != other.brightness) return false
            if (processingMs != other.processingMs) return false
            if (!boxes.contentEquals(other.boxes)) return false
            if (!scores.contentEquals(other.scores)) return false
            if (!classes.contentEquals(other.classes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = blurVar.hashCode()
            result = 31 * result + glarePercent.hashCode()
            result = 31 * result + brightness.hashCode()
            result = 31 * result + processingMs.hashCode()
            result = 31 * result + boxes.contentHashCode()
            result = 31 * result + scores.contentHashCode()
            result = 31 * result + classes.contentHashCode()
            return result
        }
    }
}
