package com.ora.wellbeing.data.mapper

import androidx.compose.ui.graphics.Color
import com.ora.wellbeing.data.model.firestore.DailyNeedCategoryDocument
import com.ora.wellbeing.domain.model.DailyNeedCategory
import timber.log.Timber

/**
 * DailyNeedCategoryMapper - Converts between Firestore DailyNeedCategoryDocument and Android DailyNeedCategory
 *
 * This mapper handles the conversion between:
 * - Backend schema (snake_case fields from OraWebApp/Firestore)
 * - Android schema (camelCase fields for Ora app)
 *
 * Key Conversions:
 * - snake_case -> camelCase (name_fr -> nameFr, is_active -> isActive)
 * - color_hex (String) -> color (Compose Color)
 * - filter_tags -> filterTags
 * - lesson_ids -> lessonIds
 */
object DailyNeedCategoryMapper {

    // Default fallback color (Ora coral orange)
    private val DEFAULT_COLOR = Color(0xFFF18D5C)

    /**
     * Converts Firestore DailyNeedCategoryDocument to Android DailyNeedCategory
     *
     * @param id Firestore document ID
     * @param doc DailyNeedCategoryDocument from Firestore (snake_case)
     * @return DailyNeedCategory for Android app (camelCase)
     */
    fun fromFirestore(id: String, doc: DailyNeedCategoryDocument): DailyNeedCategory {
        Timber.d("Mapping DailyNeedCategory from Firestore: id=$id, name_fr=${doc.name_fr}")

        return DailyNeedCategory(
            id = id.ifBlank { doc.id },
            nameFr = doc.name_fr,
            nameEn = doc.name_en.ifBlank { doc.name_fr }, // Fallback to French if no English
            descriptionFr = doc.description_fr,
            descriptionEn = doc.description_en.ifBlank { doc.description_fr }, // Fallback to French
            color = parseColorHex(doc.color_hex),
            iconUrl = doc.icon_url,
            order = doc.order,
            isActive = doc.is_active,
            filterTags = doc.filter_tags,
            lessonIds = doc.lesson_ids
        )
    }

    /**
     * Parses a HEX color string to Compose Color
     *
     * Supported formats:
     * - "#RRGGBB" (e.g., "#A78BFA")
     * - "#AARRGGBB" (e.g., "#FFA78BFA")
     * - "RRGGBB" (without #)
     *
     * @param hexString The HEX color string
     * @return Compose Color, or DEFAULT_COLOR on failure
     */
    fun parseColorHex(hexString: String?): Color {
        if (hexString.isNullOrBlank()) {
            Timber.w("parseColorHex: Empty color string, using default")
            return DEFAULT_COLOR
        }

        return try {
            // Remove # prefix if present
            val cleanHex = hexString.removePrefix("#")

            // Parse based on length
            val colorLong = when (cleanHex.length) {
                6 -> {
                    // RRGGBB format - add FF alpha
                    (0xFF000000 or cleanHex.toLong(16))
                }
                8 -> {
                    // AARRGGBB format
                    cleanHex.toLong(16)
                }
                else -> {
                    Timber.w("parseColorHex: Invalid hex length ${cleanHex.length}, using default")
                    return DEFAULT_COLOR
                }
            }

            Color(colorLong.toInt())
        } catch (e: Exception) {
            Timber.e(e, "parseColorHex: Failed to parse '$hexString', using default")
            DEFAULT_COLOR
        }
    }

    /**
     * Converts Android DailyNeedCategory back to DailyNeedCategoryDocument (for updates)
     * Note: This is primarily for future use when we allow editing from Android
     *
     * @param category DailyNeedCategory from Android app
     * @return DailyNeedCategoryDocument for Firestore
     */
    fun toFirestore(category: DailyNeedCategory): DailyNeedCategoryDocument {
        return DailyNeedCategoryDocument().apply {
            this.id = category.id
            this.name_fr = category.nameFr
            this.name_en = category.nameEn
            this.description_fr = category.descriptionFr
            this.description_en = category.descriptionEn
            this.color_hex = colorToHex(category.color)
            this.icon_url = category.iconUrl
            this.order = category.order
            this.is_active = category.isActive
            this.filter_tags = category.filterTags
            this.lesson_ids = category.lessonIds
        }
    }

    /**
     * Converts Compose Color to HEX string
     *
     * @param color The Compose Color
     * @return HEX string in "#RRGGBB" format
     */
    private fun colorToHex(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", red, green, blue)
    }

    /**
     * Creates a list of default categories for initial setup
     * Used when Firestore collection is empty
     */
    fun createDefaultCategories(): List<DailyNeedCategory> {
        return listOf(
            DailyNeedCategory(
                id = "anti-stress",
                nameFr = "Anti-stress",
                nameEn = "Anti-stress",
                descriptionFr = "Calme ton esprit et reduis ton anxiete",
                descriptionEn = "Calm your mind and reduce anxiety",
                color = DailyNeedCategory.COLOR_ANTI_STRESS,
                iconUrl = null,
                order = 0,
                isActive = true,
                filterTags = listOf("relaxation", "breathing", "meditation", "stress-relief"),
                lessonIds = emptyList()
            ),
            DailyNeedCategory(
                id = "energie-matinale",
                nameFr = "Energie matinale",
                nameEn = "Morning Energy",
                descriptionFr = "Demarre ta journee avec vitalite",
                descriptionEn = "Start your day with vitality",
                color = DailyNeedCategory.COLOR_MORNING_ENERGY,
                iconUrl = null,
                order = 1,
                isActive = true,
                filterTags = listOf("yoga", "energizing", "morning", "wake-up"),
                lessonIds = emptyList()
            ),
            DailyNeedCategory(
                id = "relaxation",
                nameFr = "Relaxation",
                nameEn = "Relaxation",
                descriptionFr = "Detends-toi et libere les tensions",
                descriptionEn = "Relax and release tension",
                color = DailyNeedCategory.COLOR_RELAXATION,
                iconUrl = null,
                order = 2,
                isActive = true,
                filterTags = listOf("stretching", "pilates", "meditation", "gentle"),
                lessonIds = emptyList()
            ),
            DailyNeedCategory(
                id = "pratique-du-soir",
                nameFr = "Pratique du soir",
                nameEn = "Evening Practice",
                descriptionFr = "Prepare ton corps et ton esprit au sommeil",
                descriptionEn = "Prepare your body and mind for sleep",
                color = DailyNeedCategory.COLOR_EVENING_PRACTICE,
                iconUrl = null,
                order = 3,
                isActive = true,
                filterTags = listOf("bedtime", "meditation", "relaxation", "sleep", "evening"),
                lessonIds = emptyList()
            )
        )
    }
}
