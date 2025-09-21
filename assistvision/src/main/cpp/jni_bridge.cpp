#include <jni.h>
#include <android/log.h>
#include "visioncpp/detection.hpp"
#include "visioncpp/pipeline.hpp"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "assistvision", __VA_ARGS__)

static jobject buildDetections(JNIEnv* env, const vision::Detections& d) {
    jclass outCls = env->FindClass("com/teamviewer/assistvision/services/nativebridge/JNIBridge$NativeDetections");
    jmethodID ctor = env->GetMethodID(outCls, "<init>", "([F[F[IDDDJ)V");
    const int n = (int)d.scores.size();
    jfloatArray jBoxes = env->NewFloatArray(n * 4);
    jfloatArray jScores = env->NewFloatArray(n);
    jintArray   jClasses= env->NewIntArray(n);
    if (n>0) {
        env->SetFloatArrayRegion(jBoxes, 0, n*4, d.boxes.data());
        env->SetFloatArrayRegion(jScores, 0, n, d.scores.data());
        env->SetIntArrayRegion  (jClasses,0, n, d.classes.data());
    }
    jobject out = env->NewObject(outCls, ctor, jBoxes, jScores, jClasses,
            (jdouble)d.blurVar, (jdouble)d.glarePercent, (jdouble)d.brightness,
            (jlong)d.processingMs);
    env->DeleteLocalRef(jBoxes); env->DeleteLocalRef(jScores); env->DeleteLocalRef(jClasses);
    return out;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeInitEmbeddedSimple(
        JNIEnv* /*env*/, jobject /*thiz*/,
        jboolean useXnnpack, jint numThreads) {

    vision::InitOptions opt;
    // GPU delegate is handled by GMS init; we do not toggle it here.
    opt.tfl.useGpu     = false; // ignored under GMS stable-ABI
    opt.tfl.useXnnpack = (useXnnpack == JNI_TRUE);
    opt.tfl.numThreads = static_cast<int>(numThreads);

    // Empty labels => use embedded labelmap.txt
    std::vector<std::string> labels;
    const bool ok = vision::pipelineInitializeEmbedded(labels, opt);
    LOGI("pipeline init (embedded/simple): %s", ok ? "OK" : "FAIL");
    return ok ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeProcessYuv420(
        JNIEnv* env, jobject /*thiz*/,
        jobject y, jobject u, jobject v,
        jint width, jint height,
        jint yRowStride, jint uRowStride, jint vRowStride,
        jint uPixStride, jint vPixStride,
        jdouble blurThr, jdouble glareThrPercent, jdouble brightFloor,
        jfloat scoreThr) {
    auto* yPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(y));
    auto* uPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(u));
    auto* vPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(v));
    vision::YuvFrame yf{ yPtr,uPtr,vPtr,(int)width,(int)height,(int)yRowStride,(int)uRowStride,(int)vRowStride,(int)uPixStride,(int)vPixStride };
    vision::ProcessConfig cfg; cfg.blurThreshold=blurThr; cfg.glareThresholdPercent=glareThrPercent; cfg.brightnessFloor=brightFloor; cfg.scoreThreshold=scoreThr;
    auto dets = vision::pipelineProcessYuv(yf, cfg);
    return buildDetections(env, dets);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeEncodeLastJpeg(
        JNIEnv* env, jobject /*thiz*/, jobject outBuffer, jint capacity, jint quality) {
    void* outPtr = env->GetDirectBufferAddress(outBuffer);
    if (!outPtr) return -1;
    std::vector<uint8_t> jpg;
    if (!vision::pipelineEncodeLastRgbaToJpeg(quality, jpg)) return -1;
    if ((int)jpg.size() > capacity) return - (jint)jpg.size();
    memcpy(outPtr, jpg.data(), jpg.size());
    return (jint)jpg.size();
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeGetLabels(
        JNIEnv* env, jobject /*thiz*/) {

    const auto& lbs = vision::pipelineLabels();
    jclass stringCls = env->FindClass("java/lang/String");
    jobjectArray arr = env->NewObjectArray((jsize)lbs.size(), stringCls, nullptr);
    for (jsize i = 0; i < (jsize)lbs.size(); ++i) {
        env->SetObjectArrayElement(arr, i, env->NewStringUTF(lbs[i].c_str()));
    }
    return arr;
}
