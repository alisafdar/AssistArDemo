# (Optional) If you reflectively access anything in your app's JNI layer
-keep class com.teamviewer.assistvision.services.nativebridge.** { *; }

# Play-services TFLite APIs
-keep class com.google.android.gms.tflite.** { *; }
-dontwarn com.google.android.gms.tflite.**

# CameraX (usually fine without rules; added here as belt & braces)
-dontwarn androidx.camera.**
