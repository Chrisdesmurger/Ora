package com.ora.wellbeing.data.model.firestore

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * YogaPoseDocument - Firestore model for yoga pose entries in lessons
 * Embedded in lessons collection as yoga_poses array
 *
 * Field names use snake_case to match OraWebApp backend schema.
 *
 * @see com.ora.wellbeing.data.mapper.YogaPoseMapper for conversion to domain model
 */
@IgnoreExtraProperties
class YogaPoseDocument() {

    // ============================================================================
    // Timing
    // ============================================================================

    /**
     * Start time of the pose in milliseconds from video start
     */
    @get:PropertyName("start_time_ms")
    @set:PropertyName("start_time_ms")
    var startTimeMs: Long = 0

    /**
     * End time of the pose in milliseconds from video start
     */
    @get:PropertyName("end_time_ms")
    @set:PropertyName("end_time_ms")
    var endTimeMs: Long = 0

    // ============================================================================
    // Identity
    // ============================================================================

    /**
     * Optional reference to yoga_poses collection for shared pose data
     */
    @get:PropertyName("pose_id")
    @set:PropertyName("pose_id")
    var poseId: String? = null

    // ============================================================================
    // Content - Title
    // ============================================================================

    /**
     * Pose title (default/fallback, usually French)
     * Ex: "Chien tête en bas"
     */
    var title: String = ""

    /**
     * French title
     */
    @get:PropertyName("title_fr")
    @set:PropertyName("title_fr")
    var titleFr: String? = null

    /**
     * English title
     */
    @get:PropertyName("title_en")
    @set:PropertyName("title_en")
    var titleEn: String? = null

    /**
     * Spanish title
     */
    @get:PropertyName("title_es")
    @set:PropertyName("title_es")
    var titleEs: String? = null

    // ============================================================================
    // Content - Stimulus/Description
    // ============================================================================

    /**
     * Short description/stimulus for the pose
     * Ex: "Étirement profond du dos et des jambes"
     */
    var stimulus: String = ""

    /**
     * French stimulus
     */
    @get:PropertyName("stimulus_fr")
    @set:PropertyName("stimulus_fr")
    var stimulusFr: String? = null

    /**
     * English stimulus
     */
    @get:PropertyName("stimulus_en")
    @set:PropertyName("stimulus_en")
    var stimulusEn: String? = null

    /**
     * Spanish stimulus
     */
    @get:PropertyName("stimulus_es")
    @set:PropertyName("stimulus_es")
    var stimulusEs: String? = null

    // ============================================================================
    // Content - Instructions
    // ============================================================================

    /**
     * Step-by-step instructions (default/fallback)
     * Ex: ["Placez les mains...", "Poussez les hanches..."]
     */
    var instructions: List<String> = emptyList()

    /**
     * French instructions
     */
    @get:PropertyName("instructions_fr")
    @set:PropertyName("instructions_fr")
    var instructionsFr: List<String>? = null

    /**
     * English instructions
     */
    @get:PropertyName("instructions_en")
    @set:PropertyName("instructions_en")
    var instructionsEn: List<String>? = null

    /**
     * Spanish instructions
     */
    @get:PropertyName("instructions_es")
    @set:PropertyName("instructions_es")
    var instructionsEs: List<String>? = null

    // ============================================================================
    // Metadata
    // ============================================================================

    /**
     * Target body zones
     * Ex: ["dos", "jambes", "épaules"]
     */
    @get:PropertyName("target_zones")
    @set:PropertyName("target_zones")
    var targetZones: List<String> = emptyList()

    /**
     * Benefits of the pose
     * Ex: ["Renforce le dos", "Améliore la flexibilité"]
     */
    var benefits: List<String>? = null

    /**
     * Side indicator for asymmetric poses
     * Values: "none", "left", "right", "both"
     */
    var side: String? = null

    /**
     * Difficulty level (1-3)
     * 1 = beginner, 2 = intermediate, 3 = advanced
     */
    var difficulty: Int? = null

    // ============================================================================
    // Visuals
    // ============================================================================

    /**
     * Thumbnail image URL for the pose
     */
    @get:PropertyName("thumbnail_url")
    @set:PropertyName("thumbnail_url")
    var thumbnailUrl: String? = null

    // ============================================================================
    // Convenience Constructor
    // ============================================================================

    constructor(
        startTimeMs: Long,
        endTimeMs: Long,
        title: String,
        stimulus: String = "",
        instructions: List<String> = emptyList(),
        targetZones: List<String> = emptyList(),
        side: String? = null,
        difficulty: Int? = null
    ) : this() {
        this.startTimeMs = startTimeMs
        this.endTimeMs = endTimeMs
        this.title = title
        this.stimulus = stimulus
        this.instructions = instructions
        this.targetZones = targetZones
        this.side = side
        this.difficulty = difficulty
    }
}
