plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization")
}

android {
    namespace = "de.coldtea.verborum.bibliotheca"
    compileSdk = Configuration.compileSdk

    defaultConfig {
        minSdk = Configuration.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // AppAuth (a transitive dependency) contributes a manifest entry with this placeholder.
        // The app module supplies the real value; the library needs one too so its own manifest —
        // now merged for Robolectric unit tests — resolves. Not used at runtime from here.
        manifestPlaceholders["appAuthRedirectScheme"] = "de.coldtea.verborum"
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
    testOptions {
        unitTests {
            // Robolectric needs the merged Android resources/manifest to run Room DAO/migration
            // integration tests in the local JVM (no emulator).
            isIncludeAndroidResources = true
        }
    }

    buildTypes {

        debug {
            isMinifyEnabled = false
            isJniDebuggable = true
        }

        release {isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    api(projects.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.animation)
    implementation(libs.material3)
    implementation(libs.foundationLayout)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(projects.core))
    // Integration testing: Robolectric (in-JVM Android) for Room DAO/migration tests,
    // MockWebServer for driving the Retrofit APIs against canned responses.
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.okhttp3.mockwebserver)
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
    ksp(libs.hilt.compiler)

    //WorkManager (background/periodic sync; Hilt-injected workers)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    //Room
    implementation(libs.room.runtime)
    implementation(libs.room.paging)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    //Retrofit
    implementation(libs.retrofit2)
    implementation(libs.okHttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    //KotlinX Serialization
    implementation(libs.kotlinx.serialization.json)
}