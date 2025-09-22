#include <jni.h>
#include <android/log.h>
#include <vector>
#include <cstring>
#include <algorithm>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include "visioncpp/frame_store.hpp"
#include "visioncpp/detection.hpp"
#include "visioncpp/pipeline.hpp"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  "assistvision", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "assistvision", __VA_ARGS__)

static jobject buildDetections(JNIEnv* env, const vision::Detections& d) {
    jclass outCls = env->FindClass("com/teamviewer/assistvision/services/nativebridge/JNIBridge$NativeDetections");
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

extern "C" JNIEXPORT jboolean JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeInitEmbeddedSimple(
        JNIEnv*, jclass,
        jboolean useXnnpack, jint numThreads) {

    vision::InitOptions opt;
    opt.tfl.useGpu     = false;
    opt.tfl.useXnnpack = (useXnnpack == JNI_TRUE);
    opt.tfl.numThreads = static_cast<int>(numThreads);

    std::vector<std::string> labels;
    const bool ok = vision::pipelineInitializeEmbedded(labels, opt);
    LOGI("pipeline init (embedded/simple): %s", ok ? "OK" : "FAIL");
    return ok ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeProcessYuv420Rotated(
        JNIEnv* env, jclass,
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
        LOGE("nativeProcessYuv420Rotated: non-direct ByteBuffer");
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
    cfg.blurThreshold         = blurThr;
    cfg.glareThresholdPercent = glareThrPercent;
    cfg.brightnessFloor       = brightFloor;
    cfg.scoreThreshold        = scoreThr;

    auto dets = vision::pipelineProcessYuvRotated(yf, cfg, static_cast<int>(rotationDeg));
    return buildDetections(env, dets);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeProcessYuv420(
        JNIEnv* env, jclass,
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
        LOGE("nativeProcessYuv420: non-direct ByteBuffer");
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
    cfg.blurThreshold         = blurThr;
    cfg.glareThresholdPercent = glareThrPercent;
    cfg.brightnessFloor       = brightFloor;
    cfg.scoreThreshold        = scoreThr;

    auto dets = vision::pipelineProcessYuv(yf, cfg);
    return buildDetections(env, dets);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeEncodeLastJpeg(
        JNIEnv* env, jobject, jobject jDirectBuffer, jint quality) {
    if (!jDirectBuffer) return 0;

    cv::Mat rgbaCopy;
    if (!vision::getLastRgbaCopy(rgbaCopy)) return 0;

    cv::Mat bgr;
    cv::cvtColor(rgbaCopy, bgr, cv::COLOR_RGBA2BGR);

    std::vector<uchar> jpeg;
    std::vector<int> params = { cv::IMWRITE_JPEG_QUALITY, std::max(0, std::min(100, (int)quality)) };
    if (!cv::imencode(".jpg", bgr, jpeg, params)) return 0;

    auto* dst = reinterpret_cast<unsigned char*>(env->GetDirectBufferAddress(jDirectBuffer));
    jlong capacity = env->GetDirectBufferCapacity(jDirectBuffer);
    if (capacity < 0 || (size_t)capacity < jpeg.size()) return -(jint)jpeg.size();
    memcpy(dst, jpeg.data(), jpeg.size());
    return (jint)jpeg.size();
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_nativeGetLabels(
        JNIEnv* env, jclass) {

    const auto& lbs = vision::pipelineLabels();
    jclass stringCls = env->FindClass("java/lang/String");
    jobjectArray arr = env->NewObjectArray(static_cast<jsize>(lbs.size()), stringCls, nullptr);
    for (jsize i = 0; i < static_cast<jsize>(lbs.size()); ++i) {
        env->SetObjectArrayElement(arr, i, env->NewStringUTF(lbs[static_cast<size_t>(i)].c_str()));
    }
    return arr;
}
