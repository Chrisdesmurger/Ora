package com.ora.wellbeing.feature.practice.player.specialized.massage.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for Text-to-Speech functionality in massage player
 *
 * Provides hands-free voice instructions for massage sessions.
 */
@Singleton
class TextToSpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private var onSpeechCompleteCallback: (() -> Unit)? = null

    init {
        initialize()
    }

    /**
     * Initialize TTS engine
     */
    fun initialize() {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set French as default language
            val result = tts?.setLanguage(Locale.FRENCH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default language
                tts?.setLanguage(Locale.getDefault())
                Timber.w("French TTS not available, using default language")
            }

            // Set up utterance progress listener
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechCompleteCallback?.invoke()
                    onSpeechCompleteCallback = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    Timber.e("TTS error for utterance: $utteranceId")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    Timber.e("TTS error: $errorCode for utterance: $utteranceId")
                }
            })

            isInitialized = true
            _isEnabled.value = true
            Timber.d("TTS initialized successfully")
        } else {
            Timber.e("TTS initialization failed with status: $status")
            isInitialized = false
            _isEnabled.value = false
        }
    }

    /**
     * Enable or disable TTS
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        if (!enabled) {
            stop()
        }
    }

    /**
     * Speak text immediately
     */
    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized || !_isEnabled.value) return

        onSpeechCompleteCallback = onComplete
        val utteranceId = UUID.randomUUID().toString()

        tts?.setSpeechRate(_speechRate.value)
        tts?.setPitch(_pitch.value)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Add text to speech queue
     */
    fun speakQueued(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized || !_isEnabled.value) return

        onSpeechCompleteCallback = onComplete
        val utteranceId = UUID.randomUUID().toString()

        tts?.setSpeechRate(_speechRate.value)
        tts?.setPitch(_pitch.value)
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
    }

    /**
     * Speak zone instruction
     */
    fun speakZoneInstruction(zoneName: String, instruction: String) {
        speak("$zoneName. $instruction")
    }

    /**
     * Speak zone change announcement
     */
    fun announceZoneChange(zoneName: String, duration: Long) {
        val minutes = (duration / 60000).toInt()
        val seconds = ((duration % 60000) / 1000).toInt()

        val durationText = when {
            minutes > 0 && seconds > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} et $seconds seconde${if (seconds > 1) "s" else ""}"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
            else -> "$seconds seconde${if (seconds > 1) "s" else ""}"
        }

        speak("Passons maintenant à la zone $zoneName. Durée: $durationText.")
    }

    /**
     * Speak pressure level change
     */
    fun announcePressureLevel(pressureName: String) {
        speak("Pression recommandée: $pressureName")
    }

    /**
     * Speak timer countdown (for last 5 seconds)
     */
    fun speakCountdown(seconds: Int) {
        if (seconds <= 5 && seconds > 0) {
            speak(seconds.toString())
        }
    }

    /**
     * Speak zone completion
     */
    fun announceZoneComplete(zoneName: String) {
        speak("Zone $zoneName terminée. Bien joué!")
    }

    /**
     * Speak session start
     */
    fun announceSessionStart(practiceTitle: String) {
        speak("Démarrage de la séance: $practiceTitle. Prenez une position confortable.")
    }

    /**
     * Speak session complete
     */
    fun announceSessionComplete(zonesCompleted: Int, totalZones: Int) {
        speak("Séance terminée! Vous avez massé $zonesCompleted zones sur $totalZones. Excellent travail!")
    }

    /**
     * Speak pause
     */
    fun announcePause() {
        speak("Session en pause. Prenez votre temps.")
    }

    /**
     * Speak resume
     */
    fun announceResume() {
        speak("Reprise de la séance.")
    }

    /**
     * Speak repetition announcement
     */
    fun announceRepetition(currentRep: Int, totalReps: Int) {
        speak("Répétition $currentRep sur $totalReps")
    }

    /**
     * Set speech rate (0.5 - 2.0)
     */
    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 2.0f)
        _speechRate.value = clampedRate
        tts?.setSpeechRate(clampedRate)
    }

    /**
     * Set pitch (0.5 - 2.0)
     */
    fun setPitch(pitch: Float) {
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        _pitch.value = clampedPitch
        tts?.setPitch(clampedPitch)
    }

    /**
     * Stop speaking
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        onSpeechCompleteCallback = null
    }

    /**
     * Check if TTS is available
     */
    fun isAvailable(): Boolean = isInitialized

    /**
     * Release TTS resources
     */
    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isEnabled.value = false
    }
}
