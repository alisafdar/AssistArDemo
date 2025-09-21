cmake_minimum_required(VERSION 3.22.1)
project("assistvision")

# ====== Version / cache base (like sample) ======
set(VISIONCPP_GIT_TAG main)

include(FetchContent)
set(FETCHCONTENT_BASE_DIR "$ENV{HOME}/.visioncpp_build.${VISIONCPP_GIT_TAG}")
set(FETCHCONTENT_UPDATES_DISCONNECTED OFF)
set(FETCHCONTENT_QUIET OFF)

# Path to a local checkout of visioncpp (set from Gradle)
set(LOCAL_VISIONCPP_REPO_DIR "${LOCAL_VISIONCPP_REPO_DIR}" CACHE PATH "Path to local visioncpp repository" FORCE)

# ====== Arch tuning (match sample) ======
if (${ANDROID_ABI} STREQUAL "arm64-v8a")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -march=armv8-a")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8-a")
elseif (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16")
endif()

# ====== Bring in the native core (visioncpp) ======
if (NOT LOCAL_VISIONCPP_REPO_DIR STREQUAL "")
    message(WARNING "Using local visioncpp repository: ${LOCAL_VISIONCPP_REPO_DIR}")
    FetchContent_Declare(
            visioncpp
            SOURCE_DIR ${LOCAL_VISIONCPP_REPO_DIR}
            EXCLUDE_FROM_ALL
    )
else()
    FetchContent_Declare(
            visioncpp
            GIT_REPOSITORY https://github.com/alisafdar/visioncpp.git
            GIT_TAG        ${VISIONCPP_GIT_TAG}
            GIT_PROGRESS   TRUE
            EXCLUDE_FROM_ALL
    )
endif()
FetchContent_MakeAvailable(visioncpp)

# ====== FlatBuffers (headers/lib only; DO NOT build flatc) ======
FetchContent_Declare(
        flatbuffers
        GIT_REPOSITORY https://github.com/google/flatbuffers.git
        # Must match GMS’ expectation to avoid ABI/version asserts
        GIT_TAG v24.3.25
        GIT_PROGRESS TRUE
        EXCLUDE_FROM_ALL
)
set(FLATBUFFERS_BUILD_FLATC OFF)
set(FLATBUFFERS_INSTALL OFF)
FetchContent_MakeAvailable(flatbuffers)

# ====== Google Play Services TFLite (stable ABI client) ======
find_package(tensorflowlite_jni_gms_client REQUIRED CONFIG)
target_compile_definitions(tensorflowlite_jni_gms_client::tensorflowlite_jni_gms_client INTERFACE
        TFLITE_IN_GMSCORE
        TFLITE_WITH_STABLE_ABI
        TFLITE_USE_OPAQUE_DELEGATE
)
target_link_libraries(tensorflowlite_jni_gms_client::tensorflowlite_jni_gms_client INTERFACE
        flatbuffers
)

# Android system log
find_library(log-lib log)

# ====== Stitch: extend the native core with TFLite GMS + flatbuffers + log ======
# (We intentionally do NOT set any options here — match sample behavior.)
target_link_libraries(visioncpp PUBLIC
        tensorflowlite_jni_gms_client::tensorflowlite_jni_gms_client
        flatbuffers
        ${log-lib}
)

# ====== JNI shim library ======
add_library(assistvision SHARED
        jni_bridge.cpp
)
target_include_directories(assistvision PRIVATE
        ${visioncpp_SOURCE_DIR}/include
)
target_link_libraries(assistvision PUBLIC
        visioncpp
        -ljnigraphics
        z
)
