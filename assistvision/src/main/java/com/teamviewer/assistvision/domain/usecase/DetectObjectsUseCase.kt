package com.teamviewer.assistvision.domain.usecase

import com.teamviewer.assistvision.services.nativebridge.JNIBridge
import com.teamviewer.assistvision.services.nativebridge.NativeObjectRecognitionService
import java.nio.ByteBuffer

data class DomainDetection(
    val label: String,
    val score: Float,
    val left: Float, val top: Float, val right: Float, val bottom: Float
)
data class DetectionResult(
    val detections: List<DomainDetection>,
    val blurVar: Double, val glarePercent: Double, val brightness: Double,
    val processingMs: Long
)

class DetectObjectsUseCase(
    private val nativeSvc: NativeObjectRecognitionService
) {
    operator fun invoke(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double,
        scoreThr: Float,
        rotationDeg: Int
    ): DetectionResult {
        val nd = JNIBridge.nativeProcessYuv420Rotated(
            y,u,v,width,height,yRowStride,uRowStride,vRowStride,uPixelStride,vPixelStride,
            blurThr,glareThrPercent,brightnessFloor,scoreThr,rotationDeg
        )
        val labels = nativeSvc.labels
        fun labelFor(id: Int): String =
            labels.getOrNull(id) ?: labels.getOrNull(id - 1) ?: "id:$id"

        val out = ArrayList<DomainDetection>(nd.scores.size)
        for (i in nd.scores.indices) {
            val l = nd.boxes[i*4+0]; val t = nd.boxes[i*4+1]
            val r = nd.boxes[i*4+2]; val b = nd.boxes[i*4+3]
            out += DomainDetection(labelFor(nd.classes[i]), nd.scores[i], l,t,r,b)
        }
        return DetectionResult(out, nd.blurVar, nd.glarePercent, nd.brightness, nd.processingMs)
    }
}
