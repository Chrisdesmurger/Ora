package com.ora.wellbeing.ui.journal

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ora.wellbeing.data.local.entities.Mood
import com.ora.wellbeing.ui.theme.OraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class JournalScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun journalScreen_displaysCorrectly() {
        // Given
        val testState = JournalUiState(
            selectedDate = LocalDate.now(),
            gratitude1 = "",
            gratitude2 = "",
            gratitude3 = "",
            selectedMood = null,
            dayStory = "",
            isLoading = false,
            errorMessage = null,
            isSaveEnabled = false
        )

        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = testState,
                    onGratitudeChanged = { _, _ -> },
                    onMoodSelected = { },
                    onDayStoryChanged = { },
                    onSaveEntry = { },
                    onDateSelected = { },
                    navigateBack = { }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Journal de gratitude")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Gratitude 1")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Gratitude 2")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Gratitude 3")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Comment était votre journée ?")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Humeur du jour")
            .assertIsDisplayed()
    }

    @Test
    fun journalScreen_gratitudeInputs_updateCorrectly() {
        // Given
        var gratitude1 = ""
        var gratitude2 = ""
        var gratitude3 = ""

        val testState = JournalUiState(
            selectedDate = LocalDate.now(),
            gratitude1 = gratitude1,
            gratitude2 = gratitude2,
            gratitude3 = gratitude3,
            selectedMood = null,
            dayStory = "",
            isLoading = false,
            errorMessage = null,
            isSaveEnabled = false
        )

        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = testState,
                    onGratitudeChanged = { index, value ->
                        when (index) {
                            0 -> gratitude1 = value
                            1 -> gratitude2 = value
                            2 -> gratitude3 = value
                        }
                    },
                    onMoodSelected = { },
                    onDayStoryChanged = { },
                    onSaveEntry = { },
                    onDateSelected = { },
                    navigateBack = { }
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Gratitude 1")
            .performTextInput("Ma famille")

        composeTestRule
            .onNodeWithText("Gratitude 2")
            .performTextInput("Ma santé")

        composeTestRule
            .onNodeWithText("Gratitude 3")
            .performTextInput("Cette belle journée")

        // Then - would need to verify state updates in real implementation
        // This test shows the structure for UI testing
    }

    @Test
    fun journalScreen_moodSelection_worksCorrectly() {
        // Given
        var selectedMood: Mood? = null
        val testState = JournalUiState(
            selectedDate = LocalDate.now(),
            gratitude1 = "",
            gratitude2 = "",
            gratitude3 = "",
            selectedMood = selectedMood,
            dayStory = "",
            isLoading = false,
            errorMessage = null,
            isSaveEnabled = false
        )

        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = testState,
                    onGratitudeChanged = { _, _ -> },
                    onMoodSelected = { mood -> selectedMood = mood },
                    onDayStoryChanged = { },
                    onSaveEntry = { },
                    onDateSelected = { },
                    navigateBack = { }
                )
            }
        }

        // When - Click on happy mood emoji
        composeTestRule
            .onNodeWithText("😊")
            .performClick()

        // Then - would verify selectedMood is updated in real implementation
    }

    @Test
    fun journalScreen_saveButton_enabledWhenFormValid() {
        // Given
        val testState = JournalUiState(
            selectedDate = LocalDate.now(),
            gratitude1 = "Ma famille",
            gratitude2 = "Ma santé",
            gratitude3 = "Cette belle journée",
            selectedMood = Mood.HAPPY,
            dayStory = "Une journée formidable",
            isLoading = false,
            errorMessage = null,
            isSaveEnabled = true
        )

        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = testState,
                    onGratitudeChanged = { _, _ -> },
                    onMoodSelected = { },
                    onDayStoryChanged = { },
                    onSaveEntry = { },
                    onDateSelected = { },
                    navigateBack = { }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Sauvegarder")
            .assertIsEnabled()
    }

    @Test
    fun journalScreen_saveButton_disabledWhenFormInvalid() {
        // Given
        val testState = JournalUiState(
            selectedDate = LocalDate.now(),
            gratitude1 = "",
            gratitude2 = "",
            gratitude3 = "",
            selectedMood = null,
            dayStory = "",
            isLoading = false,
            errorMessage = null,
            isSaveEnabled = false
        )

        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = testState,
                    onGratitudeChanged = { _, _ -> },
                    onMoodSelected = { },
                    onDayStoryChanged = { },
                    onSaveEntry = { },
                    onDateSelected = { },
                    navigateBack = { }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Sauvegarder")
            .assertIsNotEnabled()
    }

    @Test
    fun journalScreen_loadingState_displaysCorrectly() {
        // Given
        val testState = JournalUiState(
            selectedDate = LocalDate.now(),
            gratitude1 = "",
            gratitude2 = "",
            gratitude3 = "",
            selectedMood = null,
            dayStory = "",
            isLoading = true,
            errorMessage = null,
            isSaveEnabled = false
        )

        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = testState,
                    onGratitudeChanged = { _, _ -> },
                    onMoodSelected = { },
                    onDayStoryChanged = { },
                    onSaveEntry = { },
                    onDateSelected = { },
                    navigateBack = { }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Chargement")
            .assertIsDisplayed()
    }

    @Test
    fun journalScreen_errorState_displaysCorrectly() {
        // Given
        val errorMessage = "Erreur lors de la sauvegarde"
        val testState = JournalUiState(
            selectedDate = LocalDate.now(),
            gratitude1 = "",
            gratitude2 = "",
            gratitude3 = "",
            selectedMood = null,
            dayStory = "",
            isLoading = false,
            errorMessage = errorMessage,
            isSaveEnabled = false
        )

        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = testState,
                    onGratitudeChanged = { _, _ -> },
                    onMoodSelected = { },
                    onDayStoryChanged = { },
                    onSaveEntry = { },
                    onDateSelected = { },
                    navigateBack = { }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(errorMessage)
            .assertIsDisplayed()
    }

    @Test
    fun journalScreen_accessibility_labelsPresent() {
        // Given
        val testState = JournalUiState(
            selectedDate = LocalDate.now(),
            gratitude1 = "",
            gratitude2 = "",
            gratitude3 = "",
            selectedMood = null,
            dayStory = "",
            isLoading = false,
            errorMessage = null,
            isSaveEnabled = false
        )

        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = testState,
                    onGratitudeChanged = { _, _ -> },
                    onMoodSelected = { },
                    onDayStoryChanged = { },
                    onSaveEntry = { },
                    onDateSelected = { },
                    navigateBack = { }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Première gratitude")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Deuxième gratitude")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Troisième gratitude")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Sélection de l'humeur")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Récit de la journée")
            .assertIsDisplayed()
    }
}

// Sample UI State for testing
data class JournalUiState(
    val selectedDate: LocalDate,
    val gratitude1: String,
    val gratitude2: String,
    val gratitude3: String,
    val selectedMood: Mood?,
    val dayStory: String,
    val isLoading: Boolean,
    val errorMessage: String?,
    val isSaveEnabled: Boolean
)

// Sample composable function signatures for testing
@androidx.compose.runtime.Composable
fun JournalScreen(
    uiState: JournalUiState,
    onGratitudeChanged: (Int, String) -> Unit,
    onMoodSelected: (Mood) -> Unit,
    onDayStoryChanged: (String) -> Unit,
    onSaveEntry: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    navigateBack: () -> Unit
) {
    // This would be the actual implementation
    // For testing purposes, we're showing the structure
}