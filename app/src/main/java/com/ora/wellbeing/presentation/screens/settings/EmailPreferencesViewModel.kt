package com.ora.wellbeing.presentation.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.R
import com.ora.wellbeing.data.local.dao.EmailPreferencesDao
import com.ora.wellbeing.data.local.entities.EmailPreferencesEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI State for Email Preferences Screen
 *
 * Issue #55: Email preferences management screen
 */
data class EmailPreferencesUiState(
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,

    // Email preferences
    val welcomeEmails: Boolean = true,
    val engagementEmails: Boolean = true,
    val marketingEmails: Boolean = false,
    val streakReminders: Boolean = true,
    val weeklyDigest: Boolean = false,
    val language: String = "fr",

    // Last sync timestamp
    val lastSyncedAt: Long = 0
)

/**
 * UI Events for Email Preferences Screen
 */
sealed class EmailPreferencesUiEvent {
    data class ToggleWelcomeEmails(val enabled: Boolean) : EmailPreferencesUiEvent()
    data class ToggleEngagementEmails(val enabled: Boolean) : EmailPreferencesUiEvent()
    data class ToggleMarketingEmails(val enabled: Boolean) : EmailPreferencesUiEvent()
    data class ToggleStreakReminders(val enabled: Boolean) : EmailPreferencesUiEvent()
    data class ToggleWeeklyDigest(val enabled: Boolean) : EmailPreferencesUiEvent()
    data class UpdateLanguage(val language: String) : EmailPreferencesUiEvent()
    object Refresh : EmailPreferencesUiEvent()
    object DismissError : EmailPreferencesUiEvent()
    object DismissSuccess : EmailPreferencesUiEvent()
}

/**
 * ViewModel for Email Preferences Screen
 *
 * Manages email notification preferences with:
 * - Local storage in Room database
 * - Sync with remote server (OraWebApp API)
 * - Offline-first approach
 *
 * Issue #55: Email preferences management screen
 */
@HiltViewModel
class EmailPreferencesViewModel @Inject constructor(
    application: Application,
    private val emailPreferencesDao: EmailPreferencesDao,
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EmailPreferencesUiState())
    val uiState: StateFlow<EmailPreferencesUiState> = _uiState.asStateFlow()

    private val context get() = getApplication<Application>()

    init {
        loadPreferences()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: EmailPreferencesUiEvent) {
        when (event) {
            is EmailPreferencesUiEvent.ToggleWelcomeEmails -> updateWelcomeEmails(event.enabled)
            is EmailPreferencesUiEvent.ToggleEngagementEmails -> updateEngagementEmails(event.enabled)
            is EmailPreferencesUiEvent.ToggleMarketingEmails -> updateMarketingEmails(event.enabled)
            is EmailPreferencesUiEvent.ToggleStreakReminders -> updateStreakReminders(event.enabled)
            is EmailPreferencesUiEvent.ToggleWeeklyDigest -> updateWeeklyDigest(event.enabled)
            is EmailPreferencesUiEvent.UpdateLanguage -> updateLanguage(event.language)
            EmailPreferencesUiEvent.Refresh -> loadPreferences()
            EmailPreferencesUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
            EmailPreferencesUiEvent.DismissSuccess -> _uiState.update { it.copy(successMessage = null) }
        }
    }

    /**
     * Load preferences from local database
     */
    private fun loadPreferences() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = context.getString(R.string.error_must_login)
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load from local database
                val preferences = emailPreferencesDao.getEmailPreferences(uid)

                if (preferences != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            welcomeEmails = preferences.welcomeEmails,
                            engagementEmails = preferences.engagementEmails,
                            marketingEmails = preferences.marketingEmails,
                            streakReminders = preferences.streakReminders,
                            weeklyDigest = preferences.weeklyDigest,
                            language = preferences.language,
                            lastSyncedAt = preferences.lastSyncedAt
                        )
                    }
                } else {
                    // Create default preferences
                    val defaultPreferences = EmailPreferencesEntity(
                        uid = uid,
                        welcomeEmails = true,
                        engagementEmails = true,
                        marketingEmails = false,
                        streakReminders = true,
                        weeklyDigest = false,
                        language = "fr",
                        lastSyncedAt = System.currentTimeMillis()
                    )
                    emailPreferencesDao.insertEmailPreferences(defaultPreferences)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            welcomeEmails = defaultPreferences.welcomeEmails,
                            engagementEmails = defaultPreferences.engagementEmails,
                            marketingEmails = defaultPreferences.marketingEmails,
                            streakReminders = defaultPreferences.streakReminders,
                            weeklyDigest = defaultPreferences.weeklyDigest,
                            language = defaultPreferences.language,
                            lastSyncedAt = defaultPreferences.lastSyncedAt
                        )
                    }
                }

                // TODO: Sync with remote server
                // syncWithRemoteServer()

            } catch (e: Exception) {
                Timber.e(e, "Error loading email preferences")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = context.getString(R.string.email_prefs_error_loading)
                    )
                }
            }
        }
    }

    /**
     * Update welcome emails preference
     */
    private fun updateWelcomeEmails(enabled: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                emailPreferencesDao.updateWelcomeEmails(uid, enabled)
                _uiState.update { it.copy(welcomeEmails = enabled) }
                showSaveSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error updating welcome emails preference")
                showSaveError()
            }
        }
    }

    /**
     * Update engagement emails preference
     */
    private fun updateEngagementEmails(enabled: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                emailPreferencesDao.updateEngagementEmails(uid, enabled)
                _uiState.update { it.copy(engagementEmails = enabled) }
                showSaveSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error updating engagement emails preference")
                showSaveError()
            }
        }
    }

    /**
     * Update marketing emails preference
     */
    private fun updateMarketingEmails(enabled: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                emailPreferencesDao.updateMarketingEmails(uid, enabled)
                _uiState.update { it.copy(marketingEmails = enabled) }
                showSaveSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error updating marketing emails preference")
                showSaveError()
            }
        }
    }

    /**
     * Update streak reminders preference
     */
    private fun updateStreakReminders(enabled: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                emailPreferencesDao.updateStreakReminders(uid, enabled)
                _uiState.update { it.copy(streakReminders = enabled) }
                showSaveSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error updating streak reminders preference")
                showSaveError()
            }
        }
    }

    /**
     * Update weekly digest preference
     */
    private fun updateWeeklyDigest(enabled: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                emailPreferencesDao.updateWeeklyDigest(uid, enabled)
                _uiState.update { it.copy(weeklyDigest = enabled) }
                showSaveSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error updating weekly digest preference")
                showSaveError()
            }
        }
    }

    /**
     * Update email language preference
     */
    private fun updateLanguage(language: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                emailPreferencesDao.updateLanguage(uid, language)
                _uiState.update { it.copy(language = language) }
                showSaveSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error updating language preference")
                showSaveError()
            }
        }
    }

    /**
     * Show save success message
     */
    private fun showSaveSuccess() {
        _uiState.update {
            it.copy(successMessage = context.getString(R.string.email_prefs_saved))
        }
    }

    /**
     * Show save error message
     */
    private fun showSaveError() {
        _uiState.update {
            it.copy(error = context.getString(R.string.email_prefs_error_saving))
        }
    }
}
