plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization")
}

// Release gateway origin (P5-09). ".invalid" is reserved (RFC 2606): it never resolves.
val releaseApiUrl: String = (findProperty("verborumApiUrl") as String?)?.trim()
    ?.let { if (it.endsWith("/")) it else "$it/" }
    ?: "https://api.verborum.invalid/"

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
    testFixtures {
        enable = true
    }

    buildTypes {

        debug {
            // ms_gateway (P5-09) — the one origin for every backend call. It routes /dictionaries, /words,
            // /users, /marketplace and /notifications to the services; the emulator reaches the host as 10.0.2.2.
            buildConfigField("String", "ROOT_URL_VERBORUM_API", "\"http://10.0.2.2:8080/\"")
            // Keycloak is pinned to localhost:8180 on the backend (KC_HOSTNAME_URL default), so it
            // renders/redirects the browser to localhost — 10.0.2.2 would break the browser flow.
            // Reach it via `adb reverse tcp:8180 tcp:8180` so the emulator's localhost hits the host.
            buildConfigField(
                "String",
                "KEYCLOAK_ISSUER",
                "\"http://localhost:8180/realms/verborum\""
            )
            buildConfigField("String", "OAUTH_CLIENT_ID", "\"verborum-app\"")
            buildConfigField(
                "String",
                "OAUTH_REDIRECT_URI",
                "\"de.coldtea.verborum://oauth2redirect/cb\""
            )

            isMinifyEnabled = false
            isJniDebuggable = true
        }

        release {
            // The gateway origin comes from a build property, never a literal address: set verborumApiUrl in
            // ~/.gradle/gradle.properties or pass -PverborumApiUrl=https://api.<domain>/ (docs/production-cutover.md §1).
            // Unset, it is a name that cannot resolve, so a release built without it fails loudly at the first call.
            buildConfigField("String", "ROOT_URL_VERBORUM_API", "\"$releaseApiUrl\"")
            buildConfigField(
                "String",
                "KEYCLOAK_ISSUER",
                "\"http://192.168.0.241:8180/realms/verborum\""
            )
            buildConfigField("String", "OAUTH_CLIENT_ID", "\"verborum-app\"")
            buildConfigField(
                "String",
                "OAUTH_REDIRECT_URI",
                "\"de.coldtea.verborum://oauth2redirect/cb\""
            )

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
    // Pins material3 to the version the feature modules ship (the BOM alone resolves an older one).
    // Compiling core against the BOM's version while the app runs this one breaks core's
    // bottom sheets at runtime (NoSuchMethodError: rememberModalBottomSheetState).
    implementation(libs.material3)
    implementation(libs.navigation.compose)
    androidTestImplementation(platform(libs.androidx.activity.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    //Auth — encrypted token storage (EncryptedSharedPreferences backed by the Android Keystore)
    implementation(libs.androidx.security.crypto)
    //Auth — AppAuth drives the Authorization Code + PKCE flow via Custom Tabs
    implementation(libs.appauth)

    //Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    // MockWebServer drives the OkHttp auth stack (interceptor + 401→refresh authenticator).
    testImplementation(libs.okhttp3.mockwebserver)
    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.kotlinx.coroutines.test)
    testFixturesImplementation(libs.mockk)
    // Compose compiler is applied module-wide; testFixtures needs the Compose runtime on its classpath.
    testFixturesImplementation(platform(libs.androidx.activity.compose.bom))
    testFixturesImplementation(libs.androidx.compose.ui)

    //Push (P4N-09) — FCM data messages; the BoM aligns the Firebase artifact versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    //WorkManager (token registration with retry; Hilt-injected worker)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    //Retrofit
    implementation(libs.retrofit2)
    implementation(libs.okHttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    //KotlinX Serialization
    implementation(libs.kotlinx.serialization.json)
}