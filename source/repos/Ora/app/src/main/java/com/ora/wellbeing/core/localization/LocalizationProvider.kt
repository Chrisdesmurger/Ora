package com.ora.wellbeing.core.localization

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized localization provider for the Ora app.
 *
 * Manages app locale with support for:
 * - English (en) - Default
 * - French (fr)
 * - Spanish (es)
 *
 * **Usage**:
 * ```kotlin
 * // Get current locale
 * val locale = LocalizationProvider.getCurrentLocale()
 *
 * // Change locale
 * localizationProvider.setLocale("es")
 *
 * // Observe locale changes
 * localizationProvider.currentLocaleFlow.collect { locale ->
 *     // Update UI
 * }
 * ```
 *
 * **Integration with UserProfile**:
 * The user's locale preference should be saved to UserProfile.locale field
 * and loaded on app start.
 *
 * **Fallback Chain**:
 * 1. User preference (UserProfile.locale)
 * 2. System locale (device language)
 * 3. English (default)
 */
@Singleton
class LocalizationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _currentLocaleFlow = MutableStateFlow(getCurrentLocale())
    val currentLocaleFlow: StateFlow<String> = _currentLocaleFlow.asStateFlow()

    /**
     * Supported languages in the app
     */
    enum class SupportedLanguage(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        FRENCH("fr", "Français"),
        SPANISH("es", "Español");

        companion object {
            fun fromCode(code: String): SupportedLanguage? {
                return values().find { it.code.equals(code, ignoreCase = true) }
            }

            fun getAllLanguages(): List<SupportedLanguage> {
                return values().toList()
            }
        }
    }

    /**
     * Get the current app locale.
     *
     * @return Locale code ("en", "fr", "es")
     */
    fun getCurrentLocale(): String {
        val locale = getPersistedLocale() ?: getSystemLocale() ?: DEFAULT_LOCALE
        Timber.d("LocalizationProvider: Current locale = $locale")
        return locale
    }

    /**
     * Set the app locale and persist the choice.
     *
     * @param localeCode Locale code ("en", "fr", "es")
     * @return true if locale was changed successfully
     */
    fun setLocale(localeCode: String): Boolean {
        val normalized = normalizeLocaleCode(localeCode)

        if (!isSupportedLocale(normalized)) {
            Timber.w("LocalizationProvider: Unsupported locale '$localeCode', defaulting to $DEFAULT_LOCALE")
            return false
        }

        Timber.i("LocalizationProvider: Setting locale to $normalized")

        // Persist locale choice
        persistLocale(normalized)

        // Apply locale to app
        applyLocale(normalized)

        // Notify observers
        _currentLocaleFlow.value = normalized

        return true
    }

    /**
     * Apply locale to the app configuration.
     * Uses AppCompatDelegate for runtime locale switching.
     */
    private fun applyLocale(localeCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(localeCode)
        AppCompatDelegate.setApplicationLocales(localeList)
        Timber.d("LocalizationProvider: Applied locale $localeCode")
    }

    /**
     * Get system locale (device language).
     *
     * @return Locale code if supported, null otherwise
     */
    private fun getSystemLocale(): String? {
        val systemLocale = Locale.getDefault().language
        return if (isSupportedLocale(systemLocale)) systemLocale else null
    }

    /**
     * Get persisted locale from SharedPreferences.
     *
     * @return Locale code if set, null otherwise
     */
    private fun getPersistedLocale(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val locale = prefs.getString(KEY_LOCALE, null)
        Timber.d("LocalizationProvider: Persisted locale = $locale")
        return locale
    }

    /**
     * Persist locale choice to SharedPreferences.
     *
     * Note: This should ideally be synced with UserProfile.locale in Firestore.
     */
    private fun persistLocale(localeCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LOCALE, localeCode).apply()
        Timber.d("LocalizationProvider: Persisted locale = $localeCode")
    }

    /**
     * Check if locale is supported by the app.
     */
    private fun isSupportedLocale(localeCode: String): Boolean {
        return SUPPORTED_LOCALES.contains(localeCode)
    }

    /**
     * Normalize locale code (e.g., "en_US" -> "en", "FR" -> "fr")
     */
    private fun normalizeLocaleCode(localeCode: String): String {
        return localeCode.split("_", "-").first().lowercase(Locale.ROOT)
    }

    /**
     * Get the display name for a locale code.
     *
     * @param localeCode Locale code ("en", "fr", "es")
     * @return Display name (e.g., "English", "Français", "Español")
     */
    fun getDisplayName(localeCode: String): String {
        return SupportedLanguage.fromCode(localeCode)?.displayName ?: "Unknown"
    }

    companion object {
        private const val PREFS_NAME = "localization_prefs"
        private const val KEY_LOCALE = "app_locale"

        const val DEFAULT_LOCALE = "fr" // French is default (original app language)
        const val LOCALE_ENGLISH = "en"
        const val LOCALE_SPANISH = "es"

        val SUPPORTED_LOCALES = setOf(DEFAULT_LOCALE, LOCALE_ENGLISH, LOCALE_SPANISH)
    }
}

/**
 * Extension function to get localized string for models.
 *
 * **Usage**:
 * ```kotlin
 * data class ContentItem(
 *     val titleFr: String,
 *     val titleEn: String,
 *     val titleEs: String
 * ) {
 *     fun getLocalizedTitle(locale: String = LocalizationProvider.getCurrentLocale()): String {
 *         return locale.getLocalizedField(
 *             fr = titleFr,
 *             en = titleEn,
 *             es = titleEs
 *         )
 *     }
 * }
 * ```
 */
fun String.getLocalizedField(
    fr: String,
    en: String,
    es: String
): String {
    return when (this) {
        "fr" -> fr.ifEmpty { en }
        "es" -> es.ifEmpty { en }
        else -> en.ifEmpty { fr } // Default to English, fallback to French
    }
}
