package com.ora.wellbeing.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ora.wellbeing.R

/**
 * Destinations de navigation de l'application Ora
 */
sealed class OraDestinations(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    // FIX(auth): Destination d'authentification
    object Auth : OraDestinations("auth")

    // Onboarding destinations (NEW)
    object Onboarding : OraDestinations("onboarding")
    object OnboardingCelebration : OraDestinations("onboarding_celebration")

    // Main bottom navigation destinations
    object Home : OraDestinations("home")
    object Library : OraDestinations("library")
    object Journal : OraDestinations("journal")
    object Programs : OraDestinations("programs")
    object Profile : OraDestinations("profile")

    // Practice detail (NEW)
    object PracticeDetail : OraDestinations(
        route = "practice/{id}",
        arguments = listOf(
            navArgument("id") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(id: String): String {
            return "practice/$id"
        }
    }

    // Content and player destinations
    object ContentDetail : OraDestinations(
        route = "content_detail/{contentId}",
        arguments = listOf(
            navArgument("contentId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(contentId: String): String {
            return "content_detail/$contentId"
        }
    }

    object VideoPlayer : OraDestinations(
        route = "video_player/{contentId}",
        arguments = listOf(
            navArgument("contentId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(contentId: String): String {
            return "video_player/$contentId"
        }
    }

    // Journal specific destinations
    // Legacy gratitude entry (for backward compatibility)
    object JournalEntry : OraDestinations(
        route = "journal_entry/{date?}",
        arguments = listOf(
            navArgument("date") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        fun createRoute(date: String? = null): String {
            return if (date != null) "journal_entry/$date" else "journal_entry"
        }
    }

    // NEW: Daily Journal Entry (comprehensive journal)
    object DailyJournalEntry : OraDestinations(
        route = "daily_journal_entry/{date?}",
        arguments = listOf(
            navArgument("date") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        fun createRoute(date: String? = null): String {
            return if (date != null) "daily_journal_entry/$date" else "daily_journal_entry"
        }
    }

    // NEW: Journal Calendar View
    object JournalCalendar : OraDestinations("journal_calendar")

    object JournalHistory : OraDestinations("journal_history")

    // Library specific destinations
    // NEW: Library categories entry screen
    object LibraryCategories : OraDestinations("library_categories")

    // NEW: Category detail screen with filtering
    object CategoryDetail : OraDestinations(
        route = "category_detail/{categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(categoryId: String): String {
            return "category_detail/$categoryId"
        }
    }

    // NEW (Issue #37): Category detail with duration filter for Quick Sessions
    object CategoryDetailFiltered : OraDestinations(
        route = "category_detail_filtered/{categoryId}/{maxDurationMinutes}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
            },
            navArgument("maxDurationMinutes") {
                type = NavType.IntType
                defaultValue = 10
            }
        )
    ) {
        fun createRoute(categoryId: String, maxDurationMinutes: Int = 10): String {
            return "category_detail_filtered/$categoryId/$maxDurationMinutes"
        }
    }

    object LibrarySearch : OraDestinations("library_search")

    object LibraryFilters : OraDestinations("library_filters")

    // NEW (Issue #33): Daily need detail screen - filters by need_tags across all content types
    object DailyNeedDetail : OraDestinations(
        route = "daily_need_detail/{categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(categoryId: String): String {
            return "daily_need_detail/$categoryId"
        }
    }

    // Profile specific destinations
    object EditProfile : OraDestinations("edit_profile")

    // NEW (Issue #55): Email preferences screen
    object EmailPreferences : OraDestinations("email_preferences")

    object PracticeStats : OraDestinations(
        route = "practice_stats/{practiceId}",
        arguments = listOf(
            navArgument("practiceId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(practiceId: String): String {
            return "practice_stats/$practiceId"
        }
    }

    object Settings : OraDestinations("settings")
    object SettingsNotifications : OraDestinations("settings_notifications")
    object SettingsPrivacy : OraDestinations("settings_privacy")
    object StatsDetail : OraDestinations("stats_detail")
    object Badges : OraDestinations("badges")
    object About : OraDestinations("about")

    // Debug destinations
    object FirestoreDebug : OraDestinations("firestore_debug")

    // Programs specific destinations
    object ProgramDetail : OraDestinations(
        route = "program_detail/{programId}",
        arguments = listOf(
            navArgument("programId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(programId: String): String {
            return "program_detail/$programId"
        }
    }

    object ActiveProgram : OraDestinations(
        route = "active_program/{programId}",
        arguments = listOf(
            navArgument("programId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(programId: String): String {
            return "active_program/$programId"
        }
    }

    // Session completion
    object SessionComplete : OraDestinations(
        route = "session_complete/{contentId}",
        arguments = listOf(
            navArgument("contentId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(contentId: String): String {
            return "session_complete/$contentId"
        }
    }

    // Quick session launcher
    object QuickSession : OraDestinations(
        route = "quick_session/{sessionType}",
        arguments = listOf(
            navArgument("sessionType") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(sessionType: String): String {
            return "quick_session/$sessionType"
        }
    }
}

/**
 * Element de navigation pour la bottom bar
 *
 * @param route Route de navigation
 * @param labelRes String resource ID for the label (Issue #39 - Phase 1d)
 * @param selectedIcon Icon when selected
 * @param unselectedIcon Icon when unselected
 * @param hasNotification Whether to show notification badge
 */
data class BottomNavigationItem(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNotification: Boolean = false
)

/**
 * Items de la bottom navigation
 * FIX (Issue #39 - Phase 1d): Use string resources for labels
 */
val bottomNavigationItems = listOf(
    BottomNavigationItem(
        route = OraDestinations.Home.route,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavigationItem(
        route = OraDestinations.Journal.route,
        labelRes = R.string.nav_journal,
        selectedIcon = Icons.Filled.Book,
        unselectedIcon = Icons.Outlined.Book,
        hasNotification = false // Will be determined dynamically
    ),
    BottomNavigationItem(
        route = OraDestinations.Library.route,
        labelRes = R.string.nav_library,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    BottomNavigationItem(
        route = OraDestinations.Programs.route,
        labelRes = R.string.nav_programs,
        selectedIcon = Icons.Filled.CalendarToday,
        unselectedIcon = Icons.Outlined.CalendarToday
    ),
    BottomNavigationItem(
        route = OraDestinations.Profile.route,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

/**
 * Types de sessions rapides
 * Issue #37: Added AUTO_MASSAGE type
 * Maps to library categories for navigation with filtering
 *
 * FIX (Issue #39 - Phase 1d): Use string resources for display names
 */
enum class QuickSessionType(@StringRes val displayNameRes: Int, val categoryId: String) {
    BREATHING(R.string.session_breathing, "Respiration"),
    YOGA_FLASH(R.string.session_flash_yoga, "Yoga"),
    MINI_MEDITATION(R.string.session_meditation, "Meditation"),
    AUTO_MASSAGE(R.string.session_self_massage, "Auto-massage");

    companion object {
        fun fromString(value: String): QuickSessionType? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Extensions utilitaires pour la navigation
 */
object NavigationUtils {
    /**
     * Verifie si une route fait partie de la bottom navigation
     */
    fun isMainDestination(route: String?): Boolean {
        return route in bottomNavigationItems.map { it.route }
    }

    /**
     * Extrait l'ID de contenu depuis une route
     */
    fun extractContentId(route: String?): String? {
        return route?.let {
            val regex = Regex("content_detail/(.+)")
            regex.find(it)?.groupValues?.get(1)
        }
    }

    /**
     * Genere une route de contenu avec parametres optionnels
     */
    fun createContentRoute(
        contentId: String,
        programContext: String? = null,
        startPosition: Long? = null
    ): String {
        var route = OraDestinations.ContentDetail.createRoute(contentId)
        val params = mutableListOf<String>()

        programContext?.let { params.add("programContext=$it") }
        startPosition?.let { params.add("startPosition=$it") }

        if (params.isNotEmpty()) {
            route += "?" + params.joinToString("&")
        }

        return route
    }
}
