plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.teamviewer.assistar.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.teamviewer.assistar.demo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.2.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging { resources.excludes += "/META-INF/*" }
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
    implementation(libs.bundles.xml.theme)
    implementation(libs.androidx.compose.ui.graphics)

    debugImplementation(libs.bundles.debug.compose)

    implementation(project(":assistvision"))
}
