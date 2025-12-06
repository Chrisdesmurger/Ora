package com.ora.wellbeing.core.util

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ImagePreloader - Preloads category background images to avoid white flash
 *
 * Preloads images from Firebase Storage into Coil's disk cache at app startup.
 * This ensures smooth navigation without loading delays.
 *
 * Usage: Inject into MainActivity or Application class and call preloadCategoryImages()
 */
@Singleton
class ImagePreloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader
) {
    companion object {
        // Category background images from Firebase Storage
        private val CATEGORY_IMAGES = listOf(
            "https://firebasestorage.googleapis.com/v0/b/ora-wellbeing.firebasestorage.app/o/media%2Fscreen%2F1.png?alt=media&token=4b4833b9-1ab9-4ab5-b94d-bce509e60052", // Yoga
            "https://firebasestorage.googleapis.com/v0/b/ora-wellbeing.firebasestorage.app/o/media%2Fscreen%2F2.png?alt=media&token=4b10eaeb-043c-4c1e-a76d-c36e0262ff4e", // Méditation
            "https://firebasestorage.googleapis.com/v0/b/ora-wellbeing.firebasestorage.app/o/media%2Fscreen%2F3.png?alt=media&token=f9c331cd-ecf9-47bb-9bc8-d71262265837", // Pilates
            "https://firebasestorage.googleapis.com/v0/b/ora-wellbeing.firebasestorage.app/o/media%2Fscreen%2F4.png?alt=media&token=e9ca33e1-0996-435d-a6a0-e23140b4a909"  // Bien-être
        )
    }

    /**
     * Preload all category background images into Coil's cache
     * Call this from MainActivity.onCreate() or Application.onCreate()
     */
    fun preloadCategoryImages() {
        CoroutineScope(Dispatchers.IO).launch {
            Timber.d("🖼️ Starting preload of ${CATEGORY_IMAGES.size} category images")

            CATEGORY_IMAGES.forEachIndexed { index, imageUrl ->
                try {
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .build()

                    imageLoader.enqueue(request)
                    Timber.d("✅ Preloaded image ${index + 1}/${CATEGORY_IMAGES.size}")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Failed to preload image ${index + 1}: $imageUrl")
                }
            }

            Timber.d("🎉 Category images preload complete")
        }
    }
}
