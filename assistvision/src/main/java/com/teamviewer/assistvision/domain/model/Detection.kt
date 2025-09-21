package com.teamviewer.assistvision.domain.model

/**
 * One object detection in ROTATED image pixel-space.
 *
 * @param classId  Model's raw class index (0- or 1-based depending on model).
 * @param label    Human-readable label resolved from classId.
 * @param score    Confidence score [0f..1f].
 * @param left     Left   in pixels (rotated image space).
 * @param top      Top    in pixels (rotated image space).
 * @param right    Right  in pixels (rotated image space).
 * @param bottom   Bottom in pixels (rotated image space).
 */
data class Detection(
    val classId: Int,
    val label: String,
    val score: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float  get() = right - left
    val height: Float get() = bottom - top
    val area: Float   get() = (right - left) * (bottom - top)
}
