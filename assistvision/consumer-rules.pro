# Keep JNI bridge signatures so native binding works
-keep class com.teamviewer.assistvision.services.nativebridge.JNIBridge {
    public static native *;
}

# Keep Google Play services TFLite public APIs used via reflection/internal loading
-keep class com.google.android.gms.tflite.** { *; }

# Keep coroutines debug metadata (optional safety)
-keepclassmembers class kotlinx.coroutines.** {
    *;
}
-dontwarn kotlinx.coroutines.**
