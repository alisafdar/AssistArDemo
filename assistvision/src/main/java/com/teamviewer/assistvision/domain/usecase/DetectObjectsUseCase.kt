package com.teamviewer.assistvision.domain.usecase

import com.teamviewer.assistvision.services.nativebridge.NativeObjectRecognitionService
import java.nio.ByteBuffer

class DetectObjectsUseCase(private val svc: NativeObjectRecognitionService) {
    operator fun invoke(
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        width: Int, height: Int,
        yRowStride: Int, uRowStride: Int, vRowStride: Int,
        uPixelStride: Int, vPixelStride: Int,
        blurThr: Double, glareThrPercent: Double, brightnessFloor: Double, scoreThr: Float
    ) = svc.processYuv420(y,u,v,width,height, yRowStride,uRowStride,vRowStride, uPixelStride,vPixelStride,
        blurThr, glareThrPercent, brightnessFloor, scoreThr)
}
