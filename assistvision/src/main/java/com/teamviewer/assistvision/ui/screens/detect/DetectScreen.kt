package com.teamviewer.assistvision.ui.screens.detect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.teamviewer.assistvision.ui.screens.detect.components.Camera
import com.teamviewer.assistvision.ui.screens.detect.components.DetectionsOverlay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectScreen() {

    val viewModel = koinViewModel<DetectViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initializeNative()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        content = {
            Camera(
                modifier = Modifier.fillMaxSize(),
                onFps = { viewModel.onFps(it) },
                onFrame = { y, u, v, imageWidth, imageHeight, yRowStride, uRowStride, vRowStrides, uPixelStride, vPixelStride, rotationDegrees ->
                viewModel.onFrame(
                    y, u, v,
                    width = imageWidth,
                    height = imageHeight,
                    yRowStride = yRowStride,
                    uRowStride = uRowStride,
                    vRowStride = vRowStrides,
                    uPixelStride = uPixelStride,
                    vPixelStride = vPixelStride,
                    rotationDegrees = rotationDegrees
                )
            })

            DetectionsOverlay(
                modifier = Modifier.fillMaxSize(),
                width = uiState.width,
                height = uiState.height,
                items = uiState.detections
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Text(
                        text = "Blur ${"%.1f".format(uiState.blurVar)}\n" +
                                "Glare ${"%.1f".format(uiState.glarePercent)}% \n" +
                                "Bright ${"%.1f".format(uiState.brightness)}\n" +
                                "FPS: ${"%.1f".format(uiState.fps)}\n" +
                                "Duration: ${uiState.processingMs}ms",
                        color = Color.White
                    )
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        verticalArrangement = Arrangement.Bottom,
                        content = {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .background(Color.White),
                                content = {
                                    items(uiState.savedShots) { uri ->
                                        Image(
                                            modifier = Modifier
                                                .width(128.dp)
                                                .padding(6.dp),
                                            contentScale = ContentScale.Crop,
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }
                    )
                }
            )
        }
    )
}