package com.ora.wellbeing.data.mapper

import com.ora.wellbeing.data.model.firestore.YogaPoseDocument
import com.ora.wellbeing.feature.practice.player.specialized.yoga.PoseDescription
import com.ora.wellbeing.feature.practice.player.specialized.yoga.YogaSide
import com.ora.wellbeing.feature.practice.ui.Chapter
import timber.log.Timber
import java.util.Locale

/**
 * YogaPoseMapper - Converts between Firestore yoga pose data and Android domain models
 *
 * This mapper handles the conversion between:
 * - Backend schema (snake_case fields from OraWebApp)
 * - Android domain models (Chapter, PoseDescription, YogaSide)
 *
 * Key Conversions:
 * - Map<String, Any> -> YogaPoseDocument (from Firestore raw data)
 * - YogaPoseDocument -> Chapter (for seek bar navigation)
 * - YogaPoseDocument -> PoseDescription (for info cards)
 * - String -> YogaSide (for bilateral poses)
 */
object YogaPoseMapper {

    /**
     * Converts raw Firestore map data to YogaPoseDocument list
     *
     * @param rawPoses List of maps from Firestore yoga_poses field
     * @return List of YogaPoseDocument objects
     */
    fun fromFirestoreList(rawPoses: List<Map<String, Any>>?): List<YogaPoseDocument> {
        if (rawPoses.isNullOrEmpty()) {
            Timber.d("No yoga poses in document")
            return emptyList()
        }

        Timber.d("Mapping ${rawPoses.size} yoga poses from Firestore")

        return rawPoses.mapIndexed { index, map ->
            try {
                fromFirestoreMap(map)
            } catch (e: Exception) {
                Timber.e(e, "Failed to map yoga pose at index $index")
                null
            }
        }.filterNotNull()
    }

    /**
     * Converts a single Firestore map to YogaPoseDocument
     *
     * @param map Raw map data from Firestore
     * @return YogaPoseDocument object
     */
    @Suppress("UNCHECKED_CAST")
    private fun fromFirestoreMap(map: Map<String, Any>): YogaPoseDocument {
        return YogaPoseDocument().apply {
            // Timing
            startTimeMs = (map["start_time_ms"] as? Number)?.toLong() ?: 0L
            endTimeMs = (map["end_time_ms"] as? Number)?.toLong() ?: 0L

            // Identity
            poseId = map["pose_id"] as? String

            // Title (with i18n)
            title = map["title"] as? String ?: ""
            titleFr = map["title_fr"] as? String
            titleEn = map["title_en"] as? String
            titleEs = map["title_es"] as? String

            // Stimulus (with i18n)
            stimulus = map["stimulus"] as? String ?: ""
            stimulusFr = map["stimulus_fr"] as? String
            stimulusEn = map["stimulus_en"] as? String
            stimulusEs = map["stimulus_es"] as? String

            // Instructions (with i18n)
            instructions = (map["instructions"] as? List<String>) ?: emptyList()
            instructionsFr = map["instructions_fr"] as? List<String>
            instructionsEn = map["instructions_en"] as? List<String>
            instructionsEs = map["instructions_es"] as? String as? List<String>

            // Metadata
            targetZones = (map["target_zones"] as? List<String>) ?: emptyList()
            benefits = map["benefits"] as? List<String>
            side = map["side"] as? String
            difficulty = (map["difficulty"] as? Number)?.toInt()

            // Visuals
            thumbnailUrl = map["thumbnail_url"] as? String
        }
    }

    /**
     * Converts YogaPoseDocument list to Chapter list for seek bar
     *
     * @param poses List of YogaPoseDocument
     * @param languageCode Current language code (fr, en, es)
     * @return List of Chapter objects for navigation
     */
    fun toChapters(poses: List<YogaPoseDocument>, languageCode: String = "fr"): List<Chapter> {
        return poses.map { pose ->
            Chapter(
                title = getLocalizedTitle(pose, languageCode),
                startTime = pose.startTimeMs
            )
        }
    }

    /**
     * Converts YogaPoseDocument to PoseDescription for info cards
     *
     * @param pose YogaPoseDocument object
     * @param languageCode Current language code (fr, en, es)
     * @return PoseDescription for display in cards
     */
    fun toPoseDescription(pose: YogaPoseDocument, languageCode: String = "fr"): PoseDescription {
        return PoseDescription(
            stimulus = getLocalizedStimulus(pose, languageCode),
            instructions = getLocalizedInstructions(pose, languageCode),
            targetZones = pose.targetZones,
            benefits = pose.benefits
        )
    }

    /**
     * Converts side string to YogaSide enum
     *
     * @param side String value from Firestore ("none", "left", "right", "both")
     * @return YogaSide enum value
     */
    fun toYogaSide(side: String?): YogaSide {
        return when (side?.lowercase()) {
            "left" -> YogaSide.LEFT
            "right" -> YogaSide.RIGHT
            "both" -> YogaSide.LEFT // Start with left for bilateral poses
            else -> YogaSide.NONE
        }
    }

    /**
     * Checks if pose is bilateral (has both left and right sides)
     *
     * @param side String value from Firestore
     * @return True if pose needs side switching
     */
    fun isBilateral(side: String?): Boolean {
        return side?.lowercase() == "both"
    }

    // ============================================================================
    // i18n Helpers
    // ============================================================================

    /**
     * Gets localized title based on language code
     * Fallback chain: requested language -> default title
     */
    fun getLocalizedTitle(pose: YogaPoseDocument, languageCode: String = "fr"): String {
        return when (languageCode.lowercase()) {
            "fr" -> pose.titleFr ?: pose.title
            "en" -> pose.titleEn ?: pose.title
            "es" -> pose.titleEs ?: pose.title
            else -> pose.title
        }.ifBlank { pose.title }
    }

    /**
     * Gets localized stimulus based on language code
     * Fallback chain: requested language -> default stimulus
     */
    fun getLocalizedStimulus(pose: YogaPoseDocument, languageCode: String = "fr"): String {
        return when (languageCode.lowercase()) {
            "fr" -> pose.stimulusFr ?: pose.stimulus
            "en" -> pose.stimulusEn ?: pose.stimulus
            "es" -> pose.stimulusEs ?: pose.stimulus
            else -> pose.stimulus
        }.ifBlank { pose.stimulus }
    }

    /**
     * Gets localized instructions based on language code
     * Fallback chain: requested language -> default instructions
     */
    fun getLocalizedInstructions(pose: YogaPoseDocument, languageCode: String = "fr"): List<String> {
        val localized = when (languageCode.lowercase()) {
            "fr" -> pose.instructionsFr
            "en" -> pose.instructionsEn
            "es" -> pose.instructionsEs
            else -> null
        }
        return localized?.takeIf { it.isNotEmpty() } ?: pose.instructions
    }

    /**
     * Gets current device language code
     * Returns "fr", "en", or "es" (defaults to "fr")
     */
    fun getCurrentLanguageCode(): String {
        val locale = Locale.getDefault().language
        return when (locale) {
            "fr" -> "fr"
            "en" -> "en"
            "es" -> "es"
            else -> "fr" // Default to French
        }
    }
}
