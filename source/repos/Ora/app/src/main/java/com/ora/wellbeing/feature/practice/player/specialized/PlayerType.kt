package com.ora.wellbeing.feature.practice.player.specialized

import com.ora.wellbeing.core.domain.practice.Discipline
import com.ora.wellbeing.data.local.entities.ContentType

/**
 * Types de lecteurs spécialisés
 */
enum class PlayerType {
    YOGA,       // Yoga, Pilates - Vidéo avec mode miroir, chapitres postures
    MEDITATION, // Méditation, Respiration - Audio avec animation respiration
    MASSAGE;    // Bien-être/Auto-massage - Hybride avec carte corporelle

    companion object {
        fun fromDiscipline(discipline: Discipline): PlayerType {
            return when (discipline) {
                Discipline.YOGA, Discipline.PILATES -> YOGA
                Discipline.MEDITATION, Discipline.RESPIRATION -> MEDITATION
                Discipline.WELLNESS -> MASSAGE
            }
        }

        fun fromContentType(contentType: ContentType): PlayerType {
            return when (contentType) {
                ContentType.YOGA, ContentType.PILATES -> YOGA
                ContentType.MEDITATION, ContentType.BREATHING -> MEDITATION
                ContentType.SELF_MASSAGE, ContentType.BEAUTY_TIPS -> MASSAGE
            }
        }
    }
}
