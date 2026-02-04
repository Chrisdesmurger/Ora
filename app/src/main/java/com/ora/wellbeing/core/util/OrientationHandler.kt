package com.ora.wellbeing.core.util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Forces landscape orientation for the current composable scope.
 * Restores previous orientation when composable leaves composition.
 *
 * Used for yoga video playback to provide immersive 16:9 viewing experience.
 */
@Composable
fun ForceLandscapeOrientation() {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(Unit) {
        val originalOrientation = activity.requestedOrientation

        // Force landscape orientation (allows rotation between landscape modes)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            // Restore original orientation when leaving the screen
            activity.requestedOrientation = originalOrientation
        }
    }
}

/**
 * Manages immersive fullscreen mode - hides system bars during video playback.
 * System bars can be revealed by swiping from screen edges.
 *
 * @param enabled Whether immersive mode should be active
 */
@Composable
fun ImmersiveModeEffect(enabled: Boolean) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(enabled) {
        val window = activity.window
        val decorView = window.decorView

        if (enabled) {
            // Enable edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(window, false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Modern API (Android 11+)
                val controller = window.insetsController
                controller?.let {
                    it.hide(WindowInsets.Type.systemBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Legacy API
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            }
        } else {
            // Show system bars
            WindowCompat.setDecorFitsSystemWindows(window, true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.systemBars())
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }

        onDispose {
            // Always restore system bars when leaving
            WindowCompat.setDecorFitsSystemWindows(window, true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.systemBars())
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }
}

/**
 * Keeps the screen on while the composable is in composition.
 * Useful for video playback to prevent screen timeout.
 */
@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(Unit) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

/**
 * Composable that remembers and manages the controls visibility state
 * with auto-hide functionality.
 *
 * @param autoHideDelayMs Delay in milliseconds before auto-hiding controls
 * @return Pair of (showControls state, toggle function)
 */
@Composable
fun rememberControlsVisibility(
    autoHideDelayMs: Long = 3000L
): Pair<Boolean, () -> Unit> {
    var showControls by remember { mutableStateOf(true) }

    // Auto-hide is handled by the caller with LaunchedEffect + delay

    return Pair(showControls) { showControls = !showControls }
}
