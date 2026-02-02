package com.ora.wellbeing.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.R

/**
 * Email Preferences Screen
 *
 * Allows users to manage their email notification preferences:
 * - Transactional emails (always enabled, non-modifiable)
 * - Engagement emails (streak milestones, first session, program completed)
 * - Marketing emails (new content, recommendations)
 * - Weekly digest
 * - Language selector
 *
 * Issue #55: Email preferences management screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailPreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmailPreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(EmailPreferencesUiEvent.DismissError)
        }
    }

    // Show success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(EmailPreferencesUiEvent.DismissSuccess)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.email_prefs_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transactional Emails Section (always enabled)
                EmailPreferencesSection(
                    title = stringResource(R.string.email_prefs_section_transactional),
                    icon = Icons.Default.Email,
                    description = stringResource(R.string.email_prefs_section_transactional_desc)
                ) {
                    EmailPreferenceItem(
                        title = stringResource(R.string.email_prefs_welcome),
                        description = stringResource(R.string.email_prefs_welcome_desc),
                        isEnabled = true,
                        onToggle = { },
                        isLocked = true
                    )
                }

                // Engagement Emails Section
                EmailPreferencesSection(
                    title = stringResource(R.string.email_prefs_section_engagement),
                    icon = Icons.Default.TrendingUp,
                    description = stringResource(R.string.email_prefs_section_engagement_desc)
                ) {
                    EmailPreferenceItem(
                        title = stringResource(R.string.email_prefs_engagement),
                        description = stringResource(R.string.email_prefs_engagement_desc),
                        isEnabled = uiState.engagementEmails,
                        onToggle = { enabled ->
                            viewModel.onEvent(EmailPreferencesUiEvent.ToggleEngagementEmails(enabled))
                        }
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    EmailPreferenceItem(
                        title = stringResource(R.string.email_prefs_streak),
                        description = stringResource(R.string.email_prefs_streak_desc),
                        isEnabled = uiState.streakReminders,
                        onToggle = { enabled ->
                            viewModel.onEvent(EmailPreferencesUiEvent.ToggleStreakReminders(enabled))
                        }
                    )
                }

                // Marketing Emails Section
                EmailPreferencesSection(
                    title = stringResource(R.string.email_prefs_section_marketing),
                    icon = Icons.Default.Star,
                    description = stringResource(R.string.email_prefs_section_marketing_desc)
                ) {
                    EmailPreferenceItem(
                        title = stringResource(R.string.email_prefs_marketing),
                        description = stringResource(R.string.email_prefs_marketing_desc),
                        isEnabled = uiState.marketingEmails,
                        onToggle = { enabled ->
                            viewModel.onEvent(EmailPreferencesUiEvent.ToggleMarketingEmails(enabled))
                        }
                    )
                }

                // Digest Section
                EmailPreferencesSection(
                    title = stringResource(R.string.email_prefs_section_digest),
                    icon = Icons.Default.Notifications,
                    description = stringResource(R.string.email_prefs_section_digest_desc)
                ) {
                    EmailPreferenceItem(
                        title = stringResource(R.string.email_prefs_weekly_digest),
                        description = stringResource(R.string.email_prefs_weekly_digest_desc),
                        isEnabled = uiState.weeklyDigest,
                        onToggle = { enabled ->
                            viewModel.onEvent(EmailPreferencesUiEvent.ToggleWeeklyDigest(enabled))
                        }
                    )
                }

                // Language Section
                EmailPreferencesSection(
                    title = stringResource(R.string.email_prefs_section_language),
                    icon = Icons.Default.Language,
                    description = stringResource(R.string.email_prefs_section_language_desc)
                ) {
                    LanguageSelector(
                        selectedLanguage = uiState.language,
                        onLanguageSelected = { language ->
                            viewModel.onEvent(EmailPreferencesUiEvent.UpdateLanguage(language))
                        }
                    )
                }

                // Sync indicator
                if (uiState.isSyncing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.email_prefs_syncing),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Section container for email preferences
 */
@Composable
private fun EmailPreferencesSection(
    title: String,
    icon: ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Section Content
            content()
        }
    }
}

/**
 * Individual email preference toggle item
 */
@Composable
private fun EmailPreferenceItem(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    isLocked: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isLocked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        if (isLocked) {
            // Show lock icon for always-enabled items
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.email_prefs_always_enabled),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * Language selector component
 */
@Composable
private fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "fr" to stringResource(R.string.email_prefs_language_fr),
        "en" to stringResource(R.string.email_prefs_language_en),
        "es" to stringResource(R.string.email_prefs_language_es)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        languages.forEach { (code, label) ->
            LanguageChip(
                label = label,
                isSelected = selectedLanguage == code,
                onClick = { onLanguageSelected(code) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Language chip component
 */
@Composable
private fun LanguageChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
