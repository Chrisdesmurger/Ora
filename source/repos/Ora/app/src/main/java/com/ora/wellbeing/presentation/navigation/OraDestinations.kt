package com.ora.wellbeing.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Destinations de navigation de l'application Ora
 */
sealed class OraDestinations(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    // FIX(auth): Destination d'authentification
    object Auth : OraDestinations("auth")

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

    object JournalHistory : OraDestinations("journal_history")

    // Library specific destinations
    object LibrarySearch : OraDestinations("library_search")

    object LibraryFilters : OraDestinations("library_filters")

    // Profile specific destinations
    object EditProfile : OraDestinations("edit_profile")

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
 * Élément de navigation pour la bottom bar
 */
data class BottomNavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNotification: Boolean = false
)

/**
 * Items de la bottom navigation
 */
val bottomNavigationItems = listOf(
    BottomNavigationItem(
        route = OraDestinations.Home.route,
        label = "Accueil",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavigationItem(
        route = OraDestinations.Library.route,
        label = "Bibliothèque",
        selectedIcon = Icons.Filled.LibraryBooks,
        unselectedIcon = Icons.Outlined.LibraryBooks
    ),
    BottomNavigationItem(
        route = OraDestinations.Journal.route,
        label = "Journal",
        selectedIcon = Icons.Filled.Book,
        unselectedIcon = Icons.Outlined.Book,
        hasNotification = false // Will be determined dynamically
    ),
    BottomNavigationItem(
        route = OraDestinations.Programs.route,
        label = "Programmes",
        selectedIcon = Icons.Filled.CalendarToday,
        unselectedIcon = Icons.Outlined.CalendarToday
    ),
    BottomNavigationItem(
        route = OraDestinations.Profile.route,
        label = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

/**
 * Types de sessions rapides
 */
enum class QuickSessionType(val displayName: String) {
    BREATHING("Respiration Calme"),
    YOGA_FLASH("Flash Yoga"),
    MINI_MEDITATION("Mini Méditation");

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
     * Vérifie si une route fait partie de la bottom navigation
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
     * Génère une route de contenu avec paramètres optionnels
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
