package com.ora.wellbeing.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.config.FeatureFlag
import com.ora.wellbeing.data.config.FeatureFlagManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * FIX(user-dynamic): Exemple d'utilisation des Feature Flags dans un ViewModel
 *
 * Ce fichier démontre les patterns d'utilisation des feature flags.
 * À supprimer ou adapter selon les besoins réels de l'app.
 */
@HiltViewModel
class FeatureFlagsExampleViewModel @Inject constructor(
    private val featureFlagManager: FeatureFlagManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    init {
        loadFeatureStates()
    }

    /**
     * Exemple 1: Vérifier un flag au chargement
     */
    private fun loadFeatureStates() {
        viewModelScope.launch {
            // FIX(user-dynamic): Vérifier flags pour configuration UI
            val showBadges = featureFlagManager.isEnabled(FeatureFlag.BADGE_SYSTEM)
            val useDynamicLabels = featureFlagManager.isEnabled(FeatureFlag.DYNAMIC_LABELS)
            val enableOfflineMode = featureFlagManager.isEnabled(FeatureFlag.OFFLINE_MODE_ENABLED)

            _uiState.update { state ->
                state.copy(
                    showBadgeSection = showBadges,
                    usePersonalizedLabels = useDynamicLabels,
                    offlineModeEnabled = enableOfflineMode
                )
            }

            Timber.d("Feature flags loaded: badges=$showBadges, labels=$useDynamicLabels, offline=$enableOfflineMode")
        }
    }

    /**
     * Exemple 2: Gater une action risquée
     */
    fun syncToCloud() {
        viewModelScope.launch {
            // FIX(user-dynamic): Sync conditionnelle selon flag
            if (featureFlagManager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)) {
                try {
                    // Synchronisation Firestore...
                    Timber.d("Syncing stats to Firestore...")
                    _uiState.update { it.copy(syncStatus = "Synchronisé") }
                } catch (e: Exception) {
                    Timber.e(e, "Sync failed, but data safe in local Room DB")
                    _uiState.update { it.copy(syncStatus = "Erreur (données locales OK)") }
                }
            } else {
                // Flag désactivé = mode local uniquement
                Timber.d("Cloud sync disabled by feature flag, using local storage only")
                _uiState.update { it.copy(syncStatus = "Mode local uniquement") }
            }
        }
    }

    /**
     * Exemple 3: Feature toggle pour lecteur avancé
     */
    fun playVideo(videoUrl: String) {
        viewModelScope.launch {
            // FIX(user-dynamic): Utiliser lecteur simple ou avancé selon flag
            val useAdvancedPlayer = featureFlagManager.isEnabled(FeatureFlag.ADVANCED_VIDEO_PLAYER)

            if (useAdvancedPlayer) {
                // ExoPlayer avec fonctionnalités avancées (PiP, qualité adaptative)
                Timber.d("Using advanced video player with controls")
                _uiState.update { it.copy(playerType = "Avancé (ExoPlayer)") }
            } else {
                // Lecteur simple standard
                Timber.d("Using simple video player")
                _uiState.update { it.copy(playerType = "Simple") }
            }
        }
    }

    /**
     * Exemple 4: Debug - Afficher tous les flags
     */
    fun loadAllFlags() {
        viewModelScope.launch {
            val allFlags = featureFlagManager.getAllFlags()

            val flagsInfo = allFlags.entries.joinToString("\n") { (flag, enabled) ->
                val metadata = featureFlagManager.getFlagMetadata(flag)
                "${flag.key}: ${if (enabled) "ON" else "OFF"} - ${metadata?.description ?: "N/A"}"
            }

            _uiState.update { it.copy(allFlagsDebug = flagsInfo) }
            Timber.d("All feature flags:\n$flagsInfo")
        }
    }

    /**
     * Exemple 5: Personnalisation UI selon profil + flags
     */
    fun getWelcomeMessage(userLevel: String): String {
        // FIX(user-dynamic): Labels dynamiques selon profil utilisateur
        return if (featureFlagManager.isEnabled(FeatureFlag.DYNAMIC_LABELS)) {
            when (userLevel) {
                "beginner" -> "Bienvenue, débutant ! Prêt à commencer votre voyage ?"
                "intermediate" -> "Bon retour ! Poursuivez votre progression."
                "expert" -> "Namaste, maître ! Explorez les sessions avancées."
                else -> "Bienvenue sur Ora"
            }
        } else {
            // Message générique si flag désactivé
            "Bienvenue sur Ora"
        }
    }
}

/**
 * État UI pour l'exemple
 */
data class ExampleUiState(
    val showBadgeSection: Boolean = false,
    val usePersonalizedLabels: Boolean = false,
    val offlineModeEnabled: Boolean = false,
    val syncStatus: String = "En attente",
    val playerType: String = "Non déterminé",
    val allFlagsDebug: String = ""
)
