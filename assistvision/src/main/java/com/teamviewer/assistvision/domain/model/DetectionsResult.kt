package com.teamviewer.assistvision.domain.model
data class DetectionsResult(
    val detections: List<Detection>,
    val blurVar: Double,
    val glarePercent: Double,
    val brightness: Double,
    val processingMs: Long
)
