#include <jni.h>
#include <android/log.h>
#include <vector>
#include <cstring>

#include "visioncpp/detection.hpp"
#include "visioncpp/pipeline.hpp"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  "assistvision", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "assistvision", __VA_ARGS__)

// Helper: build JNIBridge.NativeDetections from vision::Detections
static jobject buildDetections(JNIEnv* env, const vision::Detections& d) {
    // Kotlin data class in: com.teamviewer.assistvision.services.nativebridge.JNIBridge.NativeDetections
    jclass outCls = env->FindClass("com/teamviewer/assistvision/services/nativebridge/JNIBridge$NativeDetections");
    // Constructor signature: ([F [F [I D D D J) V
    jmethodID ctor = env->GetMethodID(outCls, "<init>", "([F[F[IDDDJ)V");

    const jint n = static_cast<jint>(d.scores.size());
    jfloatArray jBoxes  = env->NewFloatArray(n * 4);
    jfloatArray jScores = env->NewFloatArray(n);
    jintArray   jClass  = env->NewIntArray(n);

    if (n > 0) {
        env->SetFloatArrayRegion(jBoxes,  0, n * 4, d.boxes.data());
        env->SetFloatArrayRegion(jScores, 0, n,     d.scores.data());
        env->SetIntArrayRegion  (jClass,  0, n,     d.classes.data());
    }

    jobject out = env->NewObject(
            outCls, ctor,
            jBoxes, jScores, jClass,
            static_cast<jdouble>(d.blurVar),
            static_cast<jdouble>(d.glarePercent),
            static_cast<jdouble>(d.brightness),
            static_cast<jlong>(d.processingMs)
    );

    env->DeleteLocalRef(jBoxes);
    env->DeleteLocalRef(jScores);
    env->DeleteLocalRef(jClass);
    return out;
}

/**
 * bool nativeInitEmbeddedSimple(boolean useXnnpack, int numThreads)
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeInitEmbeddedSimple(
        JNIEnv* /*env*/, jclass /*clazz*/,
        jboolean useXnnpack, jint numThreads) {

    vision::InitOptions opt;
    // GPU delegate selection is handled by GMS Lite runtime on the Java/Kotlin side.
    opt.tfl.useGpu     = false; // ignored under GMS stable ABI
    opt.tfl.useXnnpack = (useXnnpack == JNI_TRUE);
    opt.tfl.numThreads = static_cast<int>(numThreads);

    std::vector<std::string> labels; // empty -> use embedded labelmap
    const bool ok = vision::pipelineInitializeEmbedded(labels, opt);
    LOGI("pipeline init (embedded/simple): %s", ok ? "OK" : "FAIL");
    return ok ? JNI_TRUE : JNI_FALSE;
}

/**
 * NativeDetections nativeProcessYuv420Rotated(
 *   ByteBuffer y,u,v, int width,int height, int yRow,uRow,vRow, int uPix,vPix,
 *   double blurThr,double glareThr,double brightFloor, float scoreThr,
 *   int rotationDeg)
 *
 * Preferred entry: rotates in native BEFORE detection; returns pixel-space boxes
 * in the rotated image coordinates.
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeProcessYuv420Rotated(
        JNIEnv* env, jclass /*clazz*/,
        jobject y, jobject u, jobject v,
        jint width, jint height,
        jint yRowStride, jint uRowStride, jint vRowStride,
        jint uPixStride, jint vPixStride,
        jdouble blurThr, jdouble glareThrPercent, jdouble brightFloor,
        jfloat scoreThr,
        jint rotationDeg) {

    auto* yPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(y));
    auto* uPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(u));
    auto* vPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(v));
    if (!yPtr || !uPtr || !vPtr) {
        LOGE("nativeProcessYuv420Rotated: non-direct ByteBuffer!");
        vision::Detections empty;
        return buildDetections(env, empty);
    }

    vision::YuvFrame yf{
            yPtr, uPtr, vPtr,
            static_cast<int>(width),  static_cast<int>(height),
            static_cast<int>(yRowStride), static_cast<int>(uRowStride), static_cast<int>(vRowStride),
            static_cast<int>(uPixStride), static_cast<int>(vPixStride)
    };

    vision::ProcessConfig cfg;
    cfg.blurThreshold          = blurThr;
    cfg.glareThresholdPercent  = glareThrPercent;
    cfg.brightnessFloor        = brightFloor;
    cfg.scoreThreshold         = scoreThr;

    auto dets = vision::pipelineProcessYuvRotated(yf, cfg, static_cast<int>(rotationDeg));
    return buildDetections(env, dets);
}

/**
 * Legacy shim (no rotation): NativeDetections nativeProcessYuv420(...)
 * Calls rotation-aware pipeline with rotationDeg = 0.
 */
extern "C" JNIEXPORT jobject JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeProcessYuv420(
        JNIEnv* env, jclass /*clazz*/,
        jobject y, jobject u, jobject v,
        jint width, jint height,
        jint yRowStride, jint uRowStride, jint vRowStride,
        jint uPixStride, jint vPixStride,
        jdouble blurThr, jdouble glareThrPercent, jdouble brightFloor,
        jfloat scoreThr) {

    auto* yPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(y));
    auto* uPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(u));
    auto* vPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(v));
    if (!yPtr || !uPtr || !vPtr) {
        LOGE("nativeProcessYuv420: non-direct ByteBuffer!");
        vision::Detections empty;
        return buildDetections(env, empty);
    }

    vision::YuvFrame yf{
            yPtr, uPtr, vPtr,
            static_cast<int>(width),  static_cast<int>(height),
            static_cast<int>(yRowStride), static_cast<int>(uRowStride), static_cast<int>(vRowStride),
            static_cast<int>(uPixStride), static_cast<int>(vPixStride)
    };

    vision::ProcessConfig cfg;
    cfg.blurThreshold          = blurThr;
    cfg.glareThresholdPercent  = glareThrPercent;
    cfg.brightnessFloor        = brightFloor;
    cfg.scoreThreshold         = scoreThr;

    auto dets = vision::pipelineProcessYuv(yf, cfg); // calls rotated(0°) internally
    return buildDetections(env, dets);
}

/**
 * int nativeEncodeLastJpeg(ByteBuffer out, int capacity, int quality)
 * Returns positive length, or negative needed size if capacity too small, or -1 on error.
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeEncodeLastJpeg(
        JNIEnv* env, jclass /*clazz*/, jobject outBuffer, jint capacity, jint quality) {

    void* outPtr = env->GetDirectBufferAddress(outBuffer);
    if (!outPtr) {
        LOGE("nativeEncodeLastJpeg: non-direct ByteBuffer!");
        return -1;
    }
    std::vector<uint8_t> jpg;
    if (!vision::pipelineEncodeLastRgbaToJpeg(static_cast<int>(quality), jpg)) {
        return -1;
    }
    if (static_cast<int>(jpg.size()) > capacity) {
        return - static_cast<jint>(jpg.size());
    }
    std::memcpy(outPtr, jpg.data(), jpg.size());
    return static_cast<jint>(jpg.size());
}

/**
 * String[] nativeGetLabels()
 */
extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeGetLabels(
        JNIEnv* env, jclass /*clazz*/) {

    const auto& lbs = vision::pipelineLabels();
    jclass stringCls = env->FindClass("java/lang/String");
    jobjectArray arr = env->NewObjectArray(static_cast<jsize>(lbs.size()), stringCls, nullptr);
    for (jsize i = 0; i < static_cast<jsize>(lbs.size()); ++i) {
        env->SetObjectArrayElement(arr, i, env->NewStringUTF(lbs[static_cast<size_t>(i)].c_str()));
    }
    return arr;
}
