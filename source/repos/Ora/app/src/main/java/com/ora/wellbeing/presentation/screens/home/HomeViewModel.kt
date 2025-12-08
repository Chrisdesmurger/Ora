package com.ora.wellbeing.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.UserProgram
import com.ora.wellbeing.data.repository.OnboardingRepository
import com.ora.wellbeing.domain.repository.ContentRepository
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
import com.ora.wellbeing.domain.repository.ProgramRepository
import com.ora.wellbeing.domain.repository.RecommendationRepository
import com.ora.wellbeing.domain.repository.UserProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: FirestoreUserProfileRepository,
    private val userStatsRepository: FirestoreUserStatsRepository,
    private val contentRepository: ContentRepository,
    private val programRepository: ProgramRepository,
    private val userProgramRepository: UserProgramRepository,
    private val recommendationRepository: RecommendationRepository,
    private val onboardingRepository: OnboardingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHomeData()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.LoadHomeData -> observeHomeData()
        }
    }

    private fun observeHomeData() {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("observeHomeData: No authenticated user")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Vous devez etre connecte pour voir l'accueil"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Check if user has completed onboarding
                val onboardingResult = onboardingRepository.getUserOnboardingResponse(uid)
                val hasCompletedOnboarding = onboardingResult.getOrNull()?.completed == true

                // Combine all data sources for real-time personalized recommendations
                combine(
                    userProfileRepository.getUserProfile(uid),
                    userStatsRepository.getUserStats(uid),
                    contentRepository.getPopularContent(limit = 20),
                    contentRepository.getNewContent(limit = 10),
                    userProgramRepository.getActivePrograms(uid)
                ) { profile: com.ora.wellbeing.domain.model.UserProfile?,
                    stats: com.ora.wellbeing.domain.model.UserStats?,
                    popularContent: List<com.ora.wellbeing.data.model.ContentItem>,
                    newContent: List<com.ora.wellbeing.data.model.ContentItem>,
                    activePrograms: List<com.ora.wellbeing.data.model.UserProgram> ->
                    HomeData(profile, stats, popularContent, newContent, activePrograms)
                }.collect { data ->
                    val profile = data.profile
                    val stats = data.stats

                    // Get user's favorite category from stats (default to "Meditation")
                    val favoriteCategory = "Meditation" // TODO: Add category tracking to domain UserStats

                    // Build personalized recommendations (fallback if no AI recommendations)
                    val fallbackRecommendations = buildRecommendations(
                        popularContent = data.popularContent,
                        newContent = data.newContent,
                        favoriteCategory = favoriteCategory,
                        isPremium = profile?.isPremium ?: false
                    )

                    // Fetch AI-powered personalized recommendations if onboarding is complete
                    var personalizedRecommendations: List<HomeUiState.ContentRecommendation> = emptyList()
                    var showPersonalizedSection = false

                    if (hasCompletedOnboarding) {
                        try {
                            val recommendedContent = recommendationRepository.getRecommendedContent(uid, limit = 5).first()
                            if (recommendedContent.isNotEmpty()) {
                                personalizedRecommendations = recommendedContent.map { it.toContentRecommendation() }
                                showPersonalizedSection = true
                                Timber.d("HomeViewModel: Loaded ${personalizedRecommendations.size} personalized recommendations")
                            }
                        } catch (e: Exception) {
                            Timber.w(e, "HomeViewModel: Failed to load personalized recommendations, using fallback")
                        }
                    }

                    // Map active programs to UI model
                    val activeProgramsUi = data.activePrograms.map { it.toActiveProgram() }

                    _uiState.value = HomeUiState(
                        isLoading = false,
                        error = null,
                        userName = profile?.displayName() ?: "Invite",
                        streakDays = stats?.streakDays ?: 0,
                        totalMinutesThisWeek = stats?.totalMinutes ?: 0,
                        sessionsCompletedThisWeek = stats?.sessions ?: 0,
                        favoriteCategory = favoriteCategory,
                        isPremium = profile?.isPremium ?: false,
                        dailyRecommendations = fallbackRecommendations,
                        activePrograms = activeProgramsUi,
                        // NEW: Personalized recommendations from Cloud Functions
                        personalizedRecommendations = personalizedRecommendations,
                        showPersonalizedSection = showPersonalizedSection,
                        hasCompletedOnboarding = hasCompletedOnboarding
                    )

                    Timber.d("observeHomeData: Updated UI state (user=${profile?.firstName}, ${fallbackRecommendations.size} daily, ${personalizedRecommendations.size} personalized, ${activeProgramsUi.size} active programs)")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement de l'accueil: ${e.message}"
                )
                Timber.e(e, "Error observing home data")
            }
        }
    }

    /**
     * Builds personalized recommendations based on user preferences and content
     * Featured content (order < 0) always appears first
     * This is the fallback when Cloud Functions recommendations are not available
     */
    private fun buildRecommendations(
        popularContent: List<ContentItem>,
        newContent: List<ContentItem>,
        favoriteCategory: String?,
        isPremium: Boolean
    ): List<HomeUiState.ContentRecommendation> {
        // 1. PRIORITY: Extract featured content (order < 0) from all sources
        val allContent = (popularContent + newContent).distinctBy { it.id }
        val featuredContent = allContent.filter { it.order < 0 }.sortedBy { it.order }

        // 2. Build regular recommendations (excluding featured items)
        val recommendations = mutableListOf<ContentItem>()
        val featuredIds = featuredContent.map { it.id }.toSet()

        // Add content from favorite category (if available)
        if (!favoriteCategory.isNullOrEmpty() && favoriteCategory != "N/A") {
            val favoriteCategoryContent = popularContent
                .filter { it.category == favoriteCategory && it.id !in featuredIds }
                .take(2)
            recommendations.addAll(favoriteCategoryContent)
        }

        // Add new content
        val newItems = newContent
            .filter { it.id !in featuredIds }
            .take(2)
        recommendations.addAll(newItems)

        // Add popular content (excluding duplicates and featured)
        val existingIds = recommendations.map { it.id }.toSet() + featuredIds
        val popularItems = popularContent
            .filter { it.id !in existingIds }
            .take(3)
        recommendations.addAll(popularItems)

        // 3. Combine: Featured first, then regular recommendations
        val combinedRecommendations = featuredContent + recommendations

        // 4. Filter premium content if user is not premium
        val filteredRecommendations = if (!isPremium) {
            combinedRecommendations.filter { !it.isPremiumOnly }
        } else {
            combinedRecommendations
        }

        // Convert to UI model
        return filteredRecommendations
            .take(6) // Limit to 6 recommendations
            .map { it.toContentRecommendation() }
    }

    /**
     * Converts ContentItem (data model) to ContentRecommendation (UI model)
     */
    private fun ContentItem.toContentRecommendation(): HomeUiState.ContentRecommendation {
        return try {
            Timber.d("Converting ContentItem: id=$id, title=$title, thumbnailUrl=$thumbnailUrl")
            HomeUiState.ContentRecommendation(
                id = id,
                title = title,
                category = category,
                duration = duration,
                thumbnailUrl = thumbnailUrl ?: "",
                previewImageUrl = previewImageUrl,
                description = description,
                isPremiumOnly = isPremiumOnly
            )
        } catch (e: Exception) {
            Timber.e(e, "Error converting ContentItem: id=$id, title=$title")
            // Retourner une recommendation par defaut en cas d'erreur
            HomeUiState.ContentRecommendation(
                id = id.takeIf { it.isNotBlank() } ?: "unknown",
                title = title.takeIf { it.isNotBlank() } ?: "Seance sans titre",
                category = category.takeIf { it.isNotBlank() } ?: "Meditation",
                duration = duration.takeIf { it.isNotBlank() } ?: "10 min",
                thumbnailUrl = thumbnailUrl ?: "",
                description = description.takeIf { it.isNotBlank() } ?: "Decouvrez cette seance",
                isPremiumOnly = isPremiumOnly
            )
        }
    }

    /**
     * Converts UserProgram (data model) to ActiveProgram (UI model)
     */
    private fun UserProgram.toActiveProgram(): HomeUiState.ActiveProgram {
        return HomeUiState.ActiveProgram(
            id = programId,
            title = "Programme actif", // Could be enhanced by joining with Program data
            currentDay = currentDay,
            totalDays = totalDays,
            progressPercentage = calculateProgress(), // Use calculateProgress() method
            nextSessionTitle = "Session $currentDay",
            nextSessionDuration = "10 min"
        )
    }

    /**
     * Data class to hold combined home data
     */
    private data class HomeData(
        val profile: com.ora.wellbeing.domain.model.UserProfile?,
        val stats: com.ora.wellbeing.domain.model.UserStats?,
        val popularContent: List<ContentItem>,
        val newContent: List<ContentItem>,
        val activePrograms: List<UserProgram>
    )
}

/**
 * Etat de l'interface utilisateur pour l'ecran Home
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String = "Invite",
    val streakDays: Int = 0,
    val dailyRecommendations: List<ContentRecommendation> = emptyList(),
    val activePrograms: List<ActiveProgram> = emptyList(),
    val totalMinutesThisWeek: Int = 0,
    val sessionsCompletedThisWeek: Int = 0,
    val favoriteCategory: String = "N/A",
    val isPremium: Boolean = false,
    // NEW: Personalized recommendations from Cloud Functions
    val personalizedRecommendations: List<ContentRecommendation> = emptyList(),
    val showPersonalizedSection: Boolean = false,
    val hasCompletedOnboarding: Boolean = false
) {
    /**
     * Recommandation de contenu pour l'ecran d'accueil
     */
    data class ContentRecommendation(
        val id: String,
        val title: String,
        val category: String,
        val duration: String,
        val thumbnailUrl: String,
        val previewImageUrl: String? = null,
        val description: String = "",
        val isPremiumOnly: Boolean = false
    )

    /**
     * Programme actif de l'utilisateur
     */
    data class ActiveProgram(
        val id: String,
        val title: String,
        val currentDay: Int,
        val totalDays: Int,
        val progressPercentage: Int,
        val nextSessionTitle: String = "",
        val nextSessionDuration: String = ""
    )
}

/**
 * Evenements de l'interface utilisateur pour l'ecran Home
 */
sealed interface HomeUiEvent {
    data object LoadHomeData : HomeUiEvent
}
