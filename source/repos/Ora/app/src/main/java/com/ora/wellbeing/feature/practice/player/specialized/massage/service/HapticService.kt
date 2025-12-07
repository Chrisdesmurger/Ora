package com.ora.wellbeing.feature.practice.player.specialized.massage.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.ora.wellbeing.feature.practice.player.specialized.massage.PressureLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for haptic feedback in massage player
 *
 * Provides tactile feedback for:
 * - Pressure level changes
 * - Zone transitions
 * - Timer alerts
 * - Button interactions
 */
@Singleton
class HapticService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _intensity = MutableStateFlow(1.0f)
    val intensity: StateFlow<Float> = _intensity.asStateFlow()

    /**
     * Check if haptic feedback is available
     */
    fun isAvailable(): Boolean {
        return vibrator?.hasVibrator() == true
    }

    /**
     * Enable or disable haptic feedback
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }

    /**
     * Set haptic intensity (0.0 - 1.0)
     */
    fun setIntensity(intensity: Float) {
        _intensity.value = intensity.coerceIn(0.0f, 1.0f)
    }

    /**
     * Simple click feedback
     */
    fun click() {
        if (!_isEnabled.value || vibrator == null) return

        vibrate(duration = 10, amplitude = getScaledAmplitude(100))
    }

    /**
     * Double click feedback
     */
    fun doubleClick() {
        if (!_isEnabled.value || vibrator == null) return

        val pattern = longArrayOf(0, 10, 50, 10)
        val amplitudes = intArrayOf(0, getScaledAmplitude(100), 0, getScaledAmplitude(100))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Heavy click feedback
     */
    fun heavyClick() {
        if (!_isEnabled.value || vibrator == null) return

        vibrate(duration = 30, amplitude = getScaledAmplitude(200))
    }

    /**
     * Feedback for pressure level
     */
    fun vibrateForPressureLevel(level: PressureLevel) {
        if (!_isEnabled.value || vibrator == null) return

        when (level) {
            PressureLevel.LOW -> vibrateLow()
            PressureLevel.MEDIUM -> vibrateMedium()
            PressureLevel.HIGH -> vibrateHigh()
        }
    }

    /**
     * Light vibration for low pressure
     */
    private fun vibrateLow() {
        val pattern = longArrayOf(0, 50)
        val amplitudes = intArrayOf(0, getScaledAmplitude(50))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Medium vibration for medium pressure
     */
    private fun vibrateMedium() {
        val pattern = longArrayOf(0, 100, 50, 100)
        val amplitudes = intArrayOf(0, getScaledAmplitude(100), 0, getScaledAmplitude(100))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Strong vibration for high pressure
     */
    private fun vibrateHigh() {
        val pattern = longArrayOf(0, 150, 50, 150, 50, 150)
        val amplitudes = intArrayOf(0, getScaledAmplitude(200), 0, getScaledAmplitude(200), 0, getScaledAmplitude(200))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Feedback for zone change
     */
    fun vibrateZoneChange() {
        if (!_isEnabled.value || vibrator == null) return

        // Pleasant ascending pattern
        val pattern = longArrayOf(0, 50, 50, 100, 50, 150)
        val amplitudes = intArrayOf(0, getScaledAmplitude(50), 0, getScaledAmplitude(100), 0, getScaledAmplitude(150))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Feedback for zone completion
     */
    fun vibrateZoneComplete() {
        if (!_isEnabled.value || vibrator == null) return

        // Success pattern
        val pattern = longArrayOf(0, 100, 100, 100, 100, 200)
        val amplitudes = intArrayOf(0, getScaledAmplitude(100), 0, getScaledAmplitude(150), 0, getScaledAmplitude(200))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Feedback for session complete
     */
    fun vibrateSessionComplete() {
        if (!_isEnabled.value || vibrator == null) return

        // Celebratory pattern
        val pattern = longArrayOf(0, 100, 100, 100, 100, 100, 100, 300)
        val amplitudes = intArrayOf(0, getScaledAmplitude(100), 0, getScaledAmplitude(120), 0, getScaledAmplitude(140), 0, getScaledAmplitude(200))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Feedback for timer warning (last 5 seconds)
     */
    fun vibrateTimerWarning(secondsRemaining: Int) {
        if (!_isEnabled.value || vibrator == null) return

        when (secondsRemaining) {
            5, 4, 3, 2 -> vibrate(duration = 30, amplitude = getScaledAmplitude(80))
            1 -> vibrate(duration = 50, amplitude = getScaledAmplitude(120))
            0 -> vibrate(duration = 100, amplitude = getScaledAmplitude(180))
        }
    }

    /**
     * Feedback for pause
     */
    fun vibratePause() {
        if (!_isEnabled.value || vibrator == null) return

        // Descending pattern
        val pattern = longArrayOf(0, 100, 50, 50)
        val amplitudes = intArrayOf(0, getScaledAmplitude(150), 0, getScaledAmplitude(50))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Feedback for resume
     */
    fun vibrateResume() {
        if (!_isEnabled.value || vibrator == null) return

        // Ascending pattern
        val pattern = longArrayOf(0, 50, 50, 100)
        val amplitudes = intArrayOf(0, getScaledAmplitude(50), 0, getScaledAmplitude(150))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Feedback for error
     */
    fun vibrateError() {
        if (!_isEnabled.value || vibrator == null) return

        // Buzz pattern for error
        val pattern = longArrayOf(0, 100, 100, 100, 100, 100)
        val amplitudes = intArrayOf(0, getScaledAmplitude(200), 0, getScaledAmplitude(200), 0, getScaledAmplitude(200))
        vibratePattern(pattern, amplitudes)
    }

    /**
     * Continuous pulse for guidance
     */
    fun startPressureGuidancePulse(level: PressureLevel) {
        if (!_isEnabled.value || vibrator == null) return

        val amplitude = when (level) {
            PressureLevel.LOW -> 50
            PressureLevel.MEDIUM -> 100
            PressureLevel.HIGH -> 180
        }

        // Repeating pattern for pressure guidance
        val pattern = longArrayOf(0, 200, 800)
        val amplitudes = intArrayOf(0, getScaledAmplitude(amplitude), 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, amplitudes, 0)
            vibrator.vibrate(effect)
        }
    }

    /**
     * Stop any ongoing vibration
     */
    fun stop() {
        vibrator?.cancel()
    }

    private fun vibrate(duration: Long, amplitude: Int) {
        if (vibrator == null) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(duration, amplitude.coerceIn(1, 255))
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            Timber.e(e, "Haptic vibration failed")
        }
    }

    private fun vibratePattern(pattern: LongArray, amplitudes: IntArray) {
        if (vibrator == null) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmplitudes = amplitudes.map { it.coerceIn(0, 255) }.toIntArray()
                val effect = VibrationEffect.createWaveform(pattern, clampedAmplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Timber.e(e, "Haptic vibration pattern failed")
        }
    }

    private fun getScaledAmplitude(baseAmplitude: Int): Int {
        return (baseAmplitude * _intensity.value).toInt().coerceIn(1, 255)
    }
}
