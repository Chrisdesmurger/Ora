package com.ora.wellbeing.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Représente une séance de pratique complétée par l'utilisateur
 * Collection Firestore: "users/{uid}/sessions"
 *
 * Permet de tracker:
 * - L'historique complet des pratiques
 * - Les stats par type de pratique (yoga, méditation, etc.)
 * - La progression dans le temps
 */
@IgnoreExtraProperties
class PracticeSession {

    @DocumentId
    var id: String = ""

    // Référence utilisateur
    var uid: String = ""

    // Détails de la pratique
    var contentId: String = "" // ID du contenu (vidéo/audio)
    var contentTitle: String = ""
    var practiceType: String = "" // "yoga", "meditation", "pilates", "breathing"
    var discipline: String = "" // "Hatha Yoga", "Vinyasa", "Pranayama", etc.
    var level: String = "" // "beginner", "intermediate", "advanced"

    // Métriques de session
    var durationMinutes: Int = 0 // Durée réelle de la pratique
    var plannedDurationMinutes: Int = 0 // Durée prévue
    var completionPercentage: Int = 100 // 0-100
    var completed: Boolean = true

    // Timestamps
    var startedAt: Long = 0L
    var completedAt: Long = 0L
    var createdAt: Long = System.currentTimeMillis()

    // Métadonnées additionnelles
    var instructorName: String? = null
    var programId: String? = null // Si part d'un programme
    var dayInProgram: Int? = null // Jour X dans le programme

    // No-arg constructor for Firestore
    constructor()

    constructor(
        id: String = "",
        uid: String,
        contentId: String,
        contentTitle: String,
        practiceType: String,
        discipline: String,
        level: String,
        durationMinutes: Int,
        plannedDurationMinutes: Int,
        completionPercentage: Int = 100,
        completed: Boolean = true,
        startedAt: Long,
        completedAt: Long,
        createdAt: Long = System.currentTimeMillis(),
        instructorName: String? = null,
        programId: String? = null,
        dayInProgram: Int? = null
    ) : this() {
        this.id = id
        this.uid = uid
        this.contentId = contentId
        this.contentTitle = contentTitle
        this.practiceType = practiceType
        this.discipline = discipline
        this.level = level
        this.durationMinutes = durationMinutes
        this.plannedDurationMinutes = plannedDurationMinutes
        this.completionPercentage = completionPercentage
        this.completed = completed
        this.startedAt = startedAt
        this.completedAt = completedAt
        this.createdAt = createdAt
        this.instructorName = instructorName
        this.programId = programId
        this.dayInProgram = dayInProgram
    }

    @Exclude
    fun copy(
        id: String = this.id,
        uid: String = this.uid,
        contentId: String = this.contentId,
        contentTitle: String = this.contentTitle,
        practiceType: String = this.practiceType,
        discipline: String = this.discipline,
        level: String = this.level,
        durationMinutes: Int = this.durationMinutes,
        plannedDurationMinutes: Int = this.plannedDurationMinutes,
        completionPercentage: Int = this.completionPercentage,
        completed: Boolean = this.completed,
        startedAt: Long = this.startedAt,
        completedAt: Long = this.completedAt,
        createdAt: Long = this.createdAt,
        instructorName: String? = this.instructorName,
        programId: String? = this.programId,
        dayInProgram: Int? = this.dayInProgram
    ): PracticeSession {
        return PracticeSession(
            id, uid, contentId, contentTitle, practiceType, discipline, level,
            durationMinutes, plannedDurationMinutes, completionPercentage, completed,
            startedAt, completedAt, createdAt, instructorName, programId, dayInProgram
        )
    }

    companion object {
        // Types de pratique supportés
        const val TYPE_YOGA = "yoga"
        const val TYPE_MEDITATION = "meditation"
        const val TYPE_PILATES = "pilates"
        const val TYPE_BREATHING = "breathing"

        // Niveaux
        const val LEVEL_BEGINNER = "beginner"
        const val LEVEL_INTERMEDIATE = "intermediate"
        const val LEVEL_ADVANCED = "advanced"
    }
}
