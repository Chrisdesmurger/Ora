plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("androidx.room")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    // FIX(auth): Apply Google Services plugin
    id("com.google.gms.google-services")
    // FIX(user-dynamic): Detekt for code quality
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

android {
    namespace = "com.ora.wellbeing"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ora.wellbeing"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.ora.wellbeing.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
            // FIX(build-debug-android): Commented out applicationIdSuffix to match Firebase google-services.json
            // Firebase requires exact package name match: "com.ora.wellbeing" (not "com.ora.wellbeing.debug")
            // To re-enable debug suffix, add a second app entry in Firebase Console for "com.ora.wellbeing.debug"
            // applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    // FIX(build-debug-android): Configure Compose Compiler Extension Version for Kotlin 2.0.21 compatibility
    // This ensures the Kotlin Compose Compiler plugin is properly configured with Compose libraries
    // Required when using Kotlin 2.0+ with Compose 1.6.11+
    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // FIX(user-dynamic): Configuration assets pour feature flags
    sourceSets {
        getByName("main") {
            assets.srcDirs("$rootDir/config")
        }
    }

    // Test options
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        animationsDisabled = true
    }
}

// Configuration Room séparée pour éviter les conflits KSP
room {
    schemaDirectory("$projectDir/schemas")
}

// Configuration KSP spécifique
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

// FIX(user-dynamic): Configuration Detekt pour qualité de code
detekt {
    config.setFrom(files("$rootDir/config/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false

    reports {
        html {
            required.set(true)
            outputLocation.set(file("build/reports/detekt/detekt.html"))
        }
        xml {
            required.set(false)
        }
        txt {
            required.set(false)
        }
        sarif {
            required.set(false)
        }
    }
}

// FIX(user-dynamic): Task pour vérifier qualité avant commit
tasks.register("qualityCheck") {
    dependsOn("test", "lint", "detekt")
    group = "verification"
    description = "Runs all quality checks (tests, lint, detekt)"
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Compose BOM and libraries
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Dependency Injection - Séparation claire KSP Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    ksp("com.google.dagger:hilt-android-compiler:2.48.1")

    // Database - Séparation claire KSP Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Network (for future API integration)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Media & Video
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-common:1.2.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Work Manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")

    // FIX(auth): Firebase BoM and Authentication
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")

    // FIX(user-dynamic): Firebase Firestore for user data persistence
    implementation("com.google.firebase:firebase-firestore-ktx")

    // FIX(profile-edit): Firebase Storage for profile photo uploads
    implementation("com.google.firebase:firebase-storage-ktx")

    // Firebase Analytics for practice session tracking
    implementation("com.google.firebase:firebase-analytics-ktx")

    // FIX(auth): Google Sign-In with Credential Manager
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Utilities
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // Timber for logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.google.truth:truth:1.1.4")
    testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-android:1.13.8")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    kspTest("com.google.dagger:hilt-android-compiler:2.48.1")

    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.48.1")

    // Debug Tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

    // FIX(user-dynamic): Detekt plugins pour règles supplémentaires
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4")
}
