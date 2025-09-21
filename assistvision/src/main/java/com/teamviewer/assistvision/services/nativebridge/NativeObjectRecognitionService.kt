package com.teamviewer.assistvision.services.nativebridge

import android.R.string.ok
import android.content.Context
import com.teamviewer.assistvision.domain.model.Detection
import com.teamviewer.assistvision.domain.model.DetectionsResult
import java.nio.ByteBuffer

class NativeObjectRecognitionService(private val appContext: Context) {
    @Volatile private var initialized = false
    @Volatile private var labels: List<String> = emptyList()

    fun initializeEmbedded(useXnnpack: Boolean = true, numThreads: Int = 2): Boolean {
        if (initialized) return true
        initialized = JNIBridge.nativeInitEmbeddedSimple(useXnnpack, numThreads)
        if (initialized) {
            labels = JNIBridge.nativeGetLabels().toList()
        }
        return initialized
    }

    fun processYuv420(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double, scoreThr: Float
    ): DetectionsResult {
        check(initialized) { "Call initializeEmbedded() first" }
        val r = JNIBridge.nativeProcessYuv420(
            y,u,v,width,height, yRowStride,uRowStride,vRowStride, uPixelStride,vPixelStride,
            blurThr, glareThrPercent, brightnessFloor, scoreThr
        )
        val dets = buildList {
            val n = r.scores.size
            for (i in 0 until n) {
                val li = r.classes[i]
                val label = if (li in labels.indices) labels[li] else "id:$li"
                add(Detection(li, label, r.scores[i],
                    r.boxes[i*4], r.boxes[i*4+1], r.boxes[i*4+2], r.boxes[i*4+3]))
            }
        }
        return DetectionsResult(dets, r.blurVar, r.glarePercent, r.brightness, r.processingMs)
    }

    fun encodeLastJpeg(buf: ByteBuffer, quality: Int): Int =
        JNIBridge.nativeEncodeLastJpeg(buf, buf.capacity(), quality)
}
