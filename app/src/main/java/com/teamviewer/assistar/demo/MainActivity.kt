package com.teamviewer.assistar.demo

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.teamviewer.assistar.demo.detect.DetectViewModel
import com.teamviewer.assistar.demo.detect.DetectionsOverlay
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
            .launch(Manifest.permission.CAMERA)

        setContent { DetectScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectScreen(vm: DetectViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    // Make sure native pipeline is initialized once screen shows
    LaunchedEffect(Unit) { vm.initializeNative() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AssistAR Object Detection") },
                actions = {
                    Text(
                        "FPS ${"%.1f".format(state.fps)}  ${state.processingMs}ms",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                CameraPreviewYuv(
                    modifier = Modifier.fillMaxSize(),
                    onFps = { vm.onFps(it) },
                    onFrame = { y, u, v, w, h, ys, us, vs, ups, vps, rotationDeg, isFront ->
                        // NEW: pass rotation + lens facing so VM can rotate/mirror and center-crop boxes to the view
                        vm.onFrameYuv(
                            y, u, v, w, h, ys, us, vs, ups, vps,
                            rotationDeg = rotationDeg,
                            isFront = isFront
                        )
                    }
                )

                // Overlay expects view-space boxes + labels from VM
                DetectionsOverlay(
                    modifier = Modifier.fillMaxSize(),
                    width = state.width,
                    height = state.height,
                    items = state.detections
                )
            }

            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Blur ${"%.1f".format(state.blurVar)} | " +
                            "Glare ${"%.1f".format(state.glarePercent)}% | " +
                            "Bright ${"%.1f".format(state.brightness)}"
                )
                LazyRow(Modifier.fillMaxWidth().height(96.dp)) {
                    items(state.savedShots) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.width(128.dp).padding(6.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
