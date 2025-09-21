package com.teamviewer.assistvision.ui.detect.components

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraPreviewYuv(
    modifier: Modifier,
    onFps: (Double) -> Unit,
    onFrame: (
        y: ByteBuffer,
        u: ByteBuffer,
        v: ByteBuffer,
        width: Int,
        height: Int,
        yStride: Int,
        uStride: Int,
        vStride: Int,
        uPixStride: Int,
        vPixStride: Int,
        rotationDegrees: Int,
        isFrontCamera: Boolean
    ) -> Unit
) {
    val ctx = LocalContext.current
    val previewView = remember { PreviewView(ctx) }
    val provider = remember { ProcessCameraProvider.getInstance(ctx) }

    AndroidView(modifier = modifier, factory = { previewView })

    LaunchedEffect(Unit) {
        val cameraProvider = provider.get()

        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        val analyzer = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val fps = FpsCounter()
        analyzer.setAnalyzer(Dispatchers.Default.asExecutor()) { img ->
            try {
                fps.tick()
                onFps(fps.fps())

                val rotationDeg = img.imageInfo.rotationDegrees
                val isFront = (selector.lensFacing == CameraSelector.LENS_FACING_FRONT)

                onFrame(
                    img.planes[0].buffer, img.planes[1].buffer, img.planes[2].buffer,
                    img.width, img.height,
                    img.planes[0].rowStride, img.planes[1].rowStride, img.planes[2].rowStride,
                    img.planes[1].pixelStride, img.planes[2].pixelStride,
                    rotationDeg, isFront
                )
            } finally {
                img.close()
            }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            ctx as LifecycleOwner,
            selector,
            preview,
            analyzer
        )
    }
}

private class FpsCounter {
    private val last = AtomicLong(System.nanoTime())
    private var f = 0.0
    fun tick() {
        val now = System.nanoTime()
        val dt = (now - last.getAndSet(now)).coerceAtLeast(1)
        val inst = 1e9 / dt.toDouble()
        f = if (f == 0.0) inst else f * 0.9 + inst * 0.1
    }
    fun fps() = f
}
