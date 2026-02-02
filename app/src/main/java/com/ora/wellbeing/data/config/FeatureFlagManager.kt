package com.ora.wellbeing.data.config

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIX(user-dynamic): Feature Flag Manager pour gestion centralisée des flags
 *
 * Responsabilités:
 * - Chargement flags depuis config/flags.json (local-first)
 * - API simple pour vérifier état des flags
 * - Préparation Remote Config (Firebase) futur
 * - Cache en mémoire pour performance
 *
 * Usage:
 * ```
 * @Inject lateinit var featureFlagManager: FeatureFlagManager
 *
 * if (featureFlagManager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)) {
 *     // Sync stats to Firestore
 * }
 * ```
 */
@Singleton
class FeatureFlagManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    // Cache en mémoire des flags
    private var flagsCache: Map<String, FlagConfig>? = null

    // Lazy loading de la configuration
    private val flags: Map<String, FlagConfig>
        get() {
            if (flagsCache == null) {
                flagsCache = loadFlags()
            }
            return flagsCache ?: emptyMap()
        }

    /**
     * Vérifie si un feature flag est activé
     *
     * @param flag Le flag à vérifier
     * @return true si enabled, false sinon (safe default)
     */
    fun isEnabled(flag: FeatureFlag): Boolean {
        return try {
            val flagConfig = flags[flag.key]
            val enabled = flagConfig?.enabled ?: flag.defaultValue

            Timber.d("Feature flag '${flag.key}': $enabled (${flagConfig?.description ?: "default"})")
            enabled
        } catch (e: Exception) {
            Timber.e(e, "Error checking feature flag '${flag.key}', using default: ${flag.defaultValue}")
            flag.defaultValue
        }
    }

    /**
     * Récupère tous les flags avec leurs états actuels
     * Utile pour écran de debug/settings
     */
    fun getAllFlags(): Map<FeatureFlag, Boolean> {
        return FeatureFlag.entries.associateWith { isEnabled(it) }
    }

    /**
     * Récupère les métadonnées d'un flag (description, catégorie, etc.)
     */
    fun getFlagMetadata(flag: FeatureFlag): FlagConfig? {
        return flags[flag.key]
    }

    /**
     * Force le rechargement des flags (utile après Remote Config fetch)
     */
    fun reload() {
        Timber.d("Reloading feature flags...")
        flagsCache = null
        // Force reload on next access
        flags  // Trigger lazy reload
    }

    /**
     * Charge les flags depuis le fichier JSON local
     * En production, pourra être remplacé/complété par Remote Config
     */
    private fun loadFlags(): Map<String, FlagConfig> {
        return try {
            // FIX(user-dynamic): Lecture depuis assets/config/flags.json
            // Note: Le fichier est dans config/ root, copié dans assets au build
            val jsonString = context.assets.open("flags.json").bufferedReader().use { it.readText() }
            val config = json.decodeFromString<FeatureFlagConfig>(jsonString)

            Timber.d("Loaded ${config.flags.size} feature flags (version ${config.version})")
            config.flags
        } catch (e: Exception) {
            Timber.e(e, "Failed to load feature flags, using empty config")
            emptyMap()
        }
    }

    /**
     * Vérifie si Remote Config est disponible et activé
     */
    fun isRemoteConfigEnabled(): Boolean {
        return isEnabled(FeatureFlag.REMOTE_CONFIG_ENABLED)
    }

    /**
     * Active/désactive un flag (runtime override, non-persisté)
     * Utile pour tests et debug builds
     */
    @Suppress("unused")
    fun overrideFlag(flag: FeatureFlag, enabled: Boolean) {
        if (com.ora.wellbeing.BuildConfig.DEBUG) {
            val mutableFlags = flagsCache?.toMutableMap() ?: mutableMapOf()
            mutableFlags[flag.key] = FlagConfig(
                enabled = enabled,
                description = "Debug override",
                category = "debug",
                riskLevel = "low",
                addedDate = "runtime",
                comment = "Runtime override in debug mode"
            )
            flagsCache = mutableFlags
            Timber.w("DEBUG: Overriding flag '${flag.key}' = $enabled")
        } else {
            Timber.w("Flag override only available in debug builds")
        }
    }
}

/**
 * Énumération des feature flags disponibles
 * Chaque flag a une clé unique et une valeur par défaut sécurisée
 */
enum class FeatureFlag(val key: String, val defaultValue: Boolean) {
    // FIX(user-dynamic): Flags système de données utilisateur
    SYNC_STATS_FROM_ROOM("SYNC_STATS_FROM_ROOM", defaultValue = true),
    DYNAMIC_LABELS("DYNAMIC_LABELS", defaultValue = true),
    OFFLINE_MODE_ENABLED("OFFLINE_MODE_ENABLED", defaultValue = true),
    AUTO_CREATE_PROFILE("AUTO_CREATE_PROFILE", defaultValue = true),

    // Features à risque (disabled par défaut)
    ADVANCED_VIDEO_PLAYER("ADVANCED_VIDEO_PLAYER", defaultValue = false),
    NETWORK_SYNC("NETWORK_SYNC", defaultValue = false),

    // Configuration & Debug
    REMOTE_CONFIG_ENABLED("REMOTE_CONFIG_ENABLED", defaultValue = false),
    DEBUG_LOGGING("DEBUG_LOGGING", defaultValue = true),

    // Gamification & Engagement
    BADGE_SYSTEM("BADGE_SYSTEM", defaultValue = true),
    GRATITUDE_REMINDERS("GRATITUDE_REMINDERS", defaultValue = true);

    companion object {
        /**
         * Trouve un flag par sa clé string
         */
        fun fromKey(key: String): FeatureFlag? {
            return entries.find { it.key == key }
        }
    }
}

/**
 * Modèle de données pour la configuration JSON complète
 */
@Serializable
data class FeatureFlagConfig(
    val version: String,
    val lastUpdated: String? = null,
    val description: String? = null,
    val flags: Map<String, FlagConfig>,
    val metadata: FlagMetadata? = null
)

/**
 * Configuration d'un flag individuel
 */
@Serializable
data class FlagConfig(
    val enabled: Boolean,
    val description: String,
    val category: String,
    val riskLevel: String? = null,
    val addedDate: String? = null,
    val comment: String? = null
)

/**
 * Métadonnées globales de la configuration
 */
@Serializable
data class FlagMetadata(
    val totalFlags: Int? = null,
    val enabledCount: Int? = null,
    val categories: List<String>? = null,
    val riskLevels: Map<String, Int>? = null,
    val notes: List<String>? = null
)
