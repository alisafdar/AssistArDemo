package com.teamviewer.assistvision.services.nativebridge

import java.nio.ByteBuffer

object JNIBridge {
    @JvmStatic
    external fun initialize(): Boolean

    @JvmStatic
    external fun getLabels(): Array<String>

    @JvmStatic
    external fun processFrame(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blur: Double, glarePercent: Double, brightness: Double,
        score: Float,
        rotationDegrees: Int
    ): NativeDetections

    @JvmStatic
    external fun encodeFrame(buffer: ByteBuffer, quality: Int): Int

    data class NativeDetections(
        val boxes: FloatArray,    // [left, top, right, bottom] * N (pixels in rotated image space)
        val scores: FloatArray,
        val classes: IntArray,
        val blur: Double,
        val glarePercent: Double,
        val brightness: Double,
        val processingMs: Long
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NativeDetections

            if (blur != other.blur) return false
            if (glarePercent != other.glarePercent) return false
            if (brightness != other.brightness) return false
            if (processingMs != other.processingMs) return false
            if (!boxes.contentEquals(other.boxes)) return false
            if (!scores.contentEquals(other.scores)) return false
            if (!classes.contentEquals(other.classes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = blur.hashCode()
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
