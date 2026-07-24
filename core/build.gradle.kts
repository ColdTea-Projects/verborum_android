plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
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
    testFixtures {
        enable = true
    }

    buildTypes {

        debug {
            // ms_dictionary
            buildConfigField("String", "ROOT_URL_VERBORUM_API", "\"http://10.0.2.2:8085/\"")
            // ms_user
            buildConfigField("String", "ROOT_URL_VERBORUM_USER_API", "\"http://10.0.2.2:8086/\"")
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
            buildConfigField("String", "ROOT_URL_VERBORUM_API", "\"http://192.168.0.241:8085/\"")
            buildConfigField("String", "ROOT_URL_VERBORUM_USER_API", "\"http://192.168.0.241:8086/\"")
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

    //Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.kotlinx.coroutines.test)
    testFixturesImplementation(libs.mockk)
    // Compose compiler is applied module-wide; testFixtures needs the Compose runtime on its classpath.
    testFixturesImplementation(platform(libs.androidx.activity.compose.bom))
    testFixturesImplementation(libs.androidx.compose.ui)

    //Retrofit
    implementation(libs.retrofit2)
    implementation(libs.okHttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    //KotlinX Serialization
    implementation(libs.kotlinx.serialization.json)
}