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

static jobject buildDetections(JNIEnv* env, const vision::Detections& detections) {
    jclass outClass = env->FindClass("com/teamviewer/assistvision/services/nativebridge/JNIBridge$NativeDetections");
    jmethodID signature = env->GetMethodID(outClass, "<init>", "([F[F[IDDDJ)V");

    const jint n = static_cast<jint>(detections.scores.size());
    jfloatArray jBoxes  = env->NewFloatArray(n * 4);
    jfloatArray jScores = env->NewFloatArray(n);
    jintArray   jClass  = env->NewIntArray(n);

    if (n > 0) {
        env->SetFloatArrayRegion(jBoxes,  0, n * 4, detections.boxes.data());
        env->SetFloatArrayRegion(jScores, 0, n,     detections.scores.data());
        env->SetIntArrayRegion  (jClass,  0, n,     detections.classes.data());
    }

    jobject out = env->NewObject(
            outClass, signature,
            jBoxes, jScores, jClass,
            static_cast<jdouble>(detections.blur),
            static_cast<jdouble>(detections.glarePercent),
            static_cast<jdouble>(detections.brightness),
            static_cast<jlong>(detections.processingMs)
    );

    env->DeleteLocalRef(jBoxes);
    env->DeleteLocalRef(jScores);
    env->DeleteLocalRef(jClass);
    return out;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_initialize(
        JNIEnv*, jclass) {

    vision::InitOptions opt;
    opt.tfl.useGpu     = false;
    opt.tfl.useXnnpack = true;
    opt.tfl.numThreads = 2;

    const bool initialized = vision::initialize(opt);
    LOGI("pipeline init : %s", initialized ? "Success" : "FAIL");
    return initialized ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_processFrame(
        JNIEnv* env, jclass,
        jobject y, jobject u, jobject v,
        jint width, jint height,
        jint yRowStride, jint uRowStride, jint vRowStride,
        jint uPixStride, jint vPixStride,
        jdouble blur, jdouble glarePercent, jdouble brightness,
        jfloat score,
        jint rotationDegrees) {

    auto* yPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(y));
    auto* uPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(u));
    auto* vPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(v));
    if (!yPtr || !uPtr || !vPtr) {
        LOGE("nativeProcessFrame: non-direct ByteBuffer");
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
    cfg.blur         = blur;
    cfg.glarePercent = glarePercent;
    cfg.brightness       = brightness;
    cfg.score        = score;

    auto dets = vision::processFrame(yf, cfg, static_cast<int>(rotationDegrees));
    return buildDetections(env, dets);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_encodeFrame(
        JNIEnv* env, jobject, jobject jDirectBuffer, jint quality) {
    if (!jDirectBuffer) return 0;

    cv::Mat rgbaCopy;
    if (!vision::getLastRgbaCopy(rgbaCopy)) return 0;

    cv::Mat bgr;
    cv::cvtColor(rgbaCopy, bgr, cv::COLOR_RGBA2BGR);

    std::vector<uchar> jpeg;
    std::vector<int> params = { cv::IMWRITE_JPEG_QUALITY, std::max(0, std::min(100, (int)quality)) };
    if (!cv::imencode(".jpg", bgr, jpeg, params)) return 0;

    auto* destination = reinterpret_cast<unsigned char*>(env->GetDirectBufferAddress(jDirectBuffer));
    jlong capacity = env->GetDirectBufferCapacity(jDirectBuffer);
    if (capacity < 0 || (size_t)capacity < jpeg.size()) return -(jint)jpeg.size();
    memcpy(destination, jpeg.data(), jpeg.size());
    return (jint)jpeg.size();
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_teamviewer_assistvision_services_nativebridge_JNIBridge_getLabels(
        JNIEnv* env, jclass) {

    const auto& labels = vision::getLabels();
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray array = env->NewObjectArray(static_cast<jsize>(labels.size()), stringClass, nullptr);
    for (jsize i = 0; i < static_cast<jsize>(labels.size()); ++i) {
        env->SetObjectArrayElement(array, i, env->NewStringUTF(labels[static_cast<size_t>(i)].c_str()));
    }
    return array;
}
