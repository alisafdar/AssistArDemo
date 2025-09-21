import com.android.tools.r8.internal.di

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.teamviewer.assistvision"
    compileSdk = 35

    val useLocalSimpleIdentVision: Boolean =
        (rootProject.extra["useLocalSimpleIdentVision"] as? Boolean) ?: false

    val localVisionProject = rootProject.findProject(":visioncpp")
    val localPath: String =
        if (useLocalSimpleIdentVision && localVisionProject != null)
            localVisionProject.projectDir.absolutePath
        else
            ""

    defaultConfig {
        minSdk = 24
        externalNativeBuild {
            cmake {
                arguments += listOf("-DLOCAL_VISIONCPP_REPO_DIR=$localPath")
                cppFlags("-std=c++20 -O3 -flto -funroll-loops -fomit-frame-pointer -fstrict-aliasing -fvisibility=hidden")
            }
        }
        ndk { abiFilters += listOf("arm64-v8a") } //,"armeabi-v7a"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            externalNativeBuild { cmake { arguments("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON") } }
        }
    }

    externalNativeBuild {
        cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        prefab = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.ui.compose)
    implementation(libs.bundles.androidx.core)
    implementation(libs.bundles.camerax)
    implementation(libs.bundles.di)
    implementation(libs.bundles.network)
    implementation(libs.bundles.images)
    implementation(libs.bundles.tensorflow)
    implementation(libs.bundles.xml.theme)
    implementation(libs.androidx.compose.ui.graphics)

    debugImplementation(libs.bundles.debug.compose)
}
