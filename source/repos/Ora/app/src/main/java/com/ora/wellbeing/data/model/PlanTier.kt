package com.ora.wellbeing.data.model

/**
 * Plan tier enum for user subscription levels
 * Used for feature access and premium content
 */
enum class PlanTier(val value: String) {
    FREE("FREE"),
    PREMIUM("PREMIUM"),
    LIFETIME("LIFETIME");

    companion object {
        fun fromString(value: String?): PlanTier {
            return values().find { it.value == value } ?: FREE
        }
    }
}
