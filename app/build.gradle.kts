plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.pref)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.sonle.pdfscanner"
    compileSdk = 36
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "dev.sonle.pdfscanner"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }

        // Google AdMob — replace prod unit IDs in productFlavors when AdMob apps are approved.
        manifestPlaceholders["admobAppId"] = "ca-app-pub-9782307752944150~2894429670"
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
    }

    signingConfigs {
        create("release") {
            val keyAliasVar = project.findProperty("KEY_ALIAS") as String?
            val keyPasswordVar = project.findProperty("KEY_PASSWORD") as String?
            val storeFileVar = project.findProperty("STORE_FILE") as String?
            val storePasswordVar = project.findProperty("STORE_PASSWORD") as String?

            if (storeFileVar != null) {
                keyAlias = keyAliasVar
                keyPassword = keyPasswordVar
                storeFile = file(storeFileVar)
                storePassword = storePasswordVar
            }
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "LzyScan Dev")
            buildConfigField("String", "API_BASE_URL", "\"https://jsonplaceholder.typicode.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
            buildConfigField("int", "API_TIMEOUT_SECONDS", "30")
            buildConfigField("int", "MAX_ITEMS_PER_PAGE", "10")
            buildConfigField("boolean", "ENABLE_DEBUG_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_STRICT_MODE", "true")
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "false")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
            buildConfigField("boolean", "ENABLE_PERFORMANCE_MONITORING", "false")
            buildConfigField("boolean", "ENABLE_ADS", "false")
            buildConfigField("boolean", "USE_TEST_AD_UNITS", "true")
            buildConfigField("String", "PLAY_STORE_APPLICATION_ID", "\"dev.sonle.pdfscanner\"")
        }
        
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            resValue("string", "app_name", "LzyScan Staging")
            buildConfigField("String", "API_BASE_URL", "\"https://staging-api.example.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"staging\"")
            buildConfigField("int", "API_TIMEOUT_SECONDS", "15")
            buildConfigField("int", "MAX_ITEMS_PER_PAGE", "20")
            buildConfigField("boolean", "ENABLE_DEBUG_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_STRICT_MODE", "false")
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
            buildConfigField("boolean", "ENABLE_PERFORMANCE_MONITORING", "true")
            buildConfigField("boolean", "ENABLE_ADS", "false")
            buildConfigField("boolean", "USE_TEST_AD_UNITS", "true")
            buildConfigField("String", "PLAY_STORE_APPLICATION_ID", "\"dev.sonle.pdfscanner\"")
        }
        
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "LzyScan")
            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
            buildConfigField("int", "API_TIMEOUT_SECONDS", "10")
            buildConfigField("int", "MAX_ITEMS_PER_PAGE", "50")
            buildConfigField("boolean", "ENABLE_DEBUG_LOGGING", "false")
            buildConfigField("boolean", "ENABLE_STRICT_MODE", "false")
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
            buildConfigField("boolean", "ENABLE_PERFORMANCE_MONITORING", "true")
            buildConfigField("boolean", "ENABLE_ADS", "true")
            buildConfigField("boolean", "USE_TEST_AD_UNITS", "false")
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"ca-app-pub-9782307752944150/8228619625\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"ca-app-pub-9782307752944150/5842506738\"")
            buildConfigField("String", "PLAY_STORE_APPLICATION_ID", "\"dev.sonle.pdfscanner\"")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    androidResources {
        noCompress += "tflite"
    }

    testOptions {
        unitTests.all {
            it.jvmArgs(
                "-XX:+EnableDynamicAgentLoading",
                "-Djdk.attach.allowAttachSelf=true"
            )
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.iconsax.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.perf)

    // Google Ads (AdMob) + consent (UMP)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    // Google Play In-App Review
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Other
    implementation(libs.timber)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // OpenCV
    implementation(libs.opencv.android)
    implementation(libs.litert)
    implementation(libs.litert.api)
    implementation(libs.litert.support)
    implementation(libs.litert.metadata)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.room.testing)

    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    // Hilt removed

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// KSP configuration
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
dependencies {
    implementation(libs.kotlinx.serialization.json)
}
