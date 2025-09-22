package com.teamviewer.assistvision.ui.detect

import android.net.Uri

data class DetectUiState(
    val fps: Double = 0.0,
    val width: Int = 0,
    val height: Int = 0,
    val detections: List<UiDetection> = emptyList(),
    val blurVar: Double = 0.0,
    val glarePercent: Double = 0.0,
    val brightness: Double = 0.0,
    val processingMs: Long = 0,
    val savedShots: List<Uri> = emptyList()
)

data class UiDetection(
    val label: String, val score: Float, val left: Float, val top: Float, val right: Float, val bottom: Float
)