plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("androidx.room") version "2.6.1" apply false
    // FIX(auth): Add Google Services plugin for Firebase
    id("com.google.gms.google-services") version "4.4.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory.asFile)
}
