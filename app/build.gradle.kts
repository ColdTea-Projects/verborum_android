plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt") version "1.9.0"
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "de.coldtea.verborum.app"
    compileSdk = Configuration.compileSdk

    defaultConfig {
        applicationId = "de.coldtea.verborum.app"
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
        versionCode = Configuration.versionCode
        versionName =  Configuration.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    api(projects.core)
    implementation(projects.bibliotheca)
    implementation(projects.forum)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

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

    //Room
    implementation(libs.room.runtime)
    implementation(libs.room.paging)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    //Retrofit
    implementation(libs.retrofit2)
    implementation(libs.okHttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    //KotlinX Serialization
    implementation(libs.kotlinx.serialization.json)
}

kapt {
    correctErrorTypes = true
    javacOptions {
        option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
    }
}