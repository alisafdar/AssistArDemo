package com.teamviewer.assistvision.domain.model

data class DetectionsResult(
    val detections: List<Detection>,
    val blur: Double,
    val glarePercent: Double,
    val brightness: Double,
    val processingMs: Long
)

data class DetectionResult(
    val detections: List<DomainDetection>,
    val blur: Double,
    val glarePercent: Double,
    val brightness: Double,
    val processingDuration: Long
)
