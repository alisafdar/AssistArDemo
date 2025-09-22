package com.teamviewer.assistvision.ui.screens.detect.components

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

@Composable
fun Camera(
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
        rotationDegrees: Int
    ) -> Unit
) {
    val localContext = LocalContext.current
    val previewView = remember { PreviewView(localContext) }
    val provider = remember { ProcessCameraProvider.getInstance(localContext) }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )

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
        analyzer.setAnalyzer(Dispatchers.Default.asExecutor()) { frame ->
            try {
                fps.tick()
                onFps(fps.fps())

                val rotationDeg = frame.imageInfo.rotationDegrees

                onFrame(
                    frame.planes[0].buffer, frame.planes[1].buffer, frame.planes[2].buffer,
                    frame.width, frame.height,
                    frame.planes[0].rowStride, frame.planes[1].rowStride, frame.planes[2].rowStride,
                    frame.planes[1].pixelStride, frame.planes[2].pixelStride,
                    rotationDeg
                )
            } finally {
                frame.close()
            }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            localContext as LifecycleOwner,
            selector,
            preview,
            analyzer
        )
    }
}

private class FpsCounter {
    private val last = AtomicLong(System.nanoTime())
    private var fps = 0.0
    fun tick() {
        val now = System.nanoTime()
        val time = (now - last.getAndSet(now)).coerceAtLeast(1)
        val instance = 1e9 / time.toDouble()
        fps = if (fps == 0.0) instance else fps * 0.9 + instance * 0.1
    }
    fun fps() = fps
}
