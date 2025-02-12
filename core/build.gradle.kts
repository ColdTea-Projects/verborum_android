plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    kotlin("kapt") version "2.0.0"
}

android {
    namespace = "de.coldtea.verborum.core"
    compileSdk = Configuration.compileSdk

    defaultConfig {
        minSdk = Configuration.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    buildTypes {

        debug {
            buildConfigField("String", "ROOT_URL_VERBORUM_API", "\"http://192.168.0.241:8085/\"")

            isMinifyEnabled = false
            isJniDebuggable = true
        }

        release {
            buildConfigField("String", "ROOT_URL_VERBORUM_API", "\"http://192.168.0.241:8085/\"")

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    //Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.activity.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.navigation.compose)
    androidTestImplementation(platform(libs.androidx.activity.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    //Retrofit
    implementation(libs.retrofit2)
    implementation(libs.okHttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    //KotlinX Serialization
    implementation(libs.kotlinx.serialization.json)
}