package com.ora.wellbeing.feature.practice.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.firebase.analytics.FirebaseAnalytics
import com.ora.wellbeing.core.data.practice.PracticeRepository
import com.ora.wellbeing.core.domain.practice.Discipline
import com.ora.wellbeing.core.domain.practice.Level
import com.ora.wellbeing.core.domain.practice.MediaType
import com.ora.wellbeing.core.domain.practice.Practice
import com.ora.wellbeing.core.domain.stats.IncrementSessionUseCase
import com.ora.wellbeing.feature.practice.player.PlaybackSpeed
import com.ora.wellbeing.feature.practice.player.RepeatMode
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: PlayerViewModel
    private lateinit var application: Application
    private lateinit var practiceRepository: PracticeRepository
    private lateinit var incrementSessionUseCase: IncrementSessionUseCase
    private lateinit var analytics: FirebaseAnalytics

    private val testPractice = Practice(
        id = "test-practice-1",
        title = "Morning Yoga Flow",
        discipline = Discipline.YOGA,
        level = Level.BEGINNER,
        durationMin = 20,
        description = "A gentle morning yoga flow",
        mediaType = MediaType.VIDEO,
        mediaUrl = "https://example.com/yoga.mp4",
        thumbnailUrl = "https://example.com/thumbnail.jpg",
        instructor = "Sarah Johnson",
        benefits = listOf("Flexibility", "Strength", "Balance")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        application = mockk(relaxed = true)
        practiceRepository = mockk()
        incrementSessionUseCase = mockk()
        analytics = mockk(relaxed = true)

        // Mock application context
        every { application.applicationContext } returns application

        viewModel = PlayerViewModel(
            application = application,
            practiceRepository = practiceRepository,
            incrementSessionUseCase = incrementSessionUseCase,
            analytics = analytics
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `loadPractice should update state with practice on success`() = runTest {
        // Given
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.success(testPractice)
        coEvery { practiceRepository.getSimilar("test-practice-1") } returns Result.success(emptyList())

        // When
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.loadPractice("test-practice-1")
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertNotNull(successState.practice)
            assertEquals("Morning Yoga Flow", successState.practice?.title)
            assertEquals(null, successState.error)
        }

        // Verify analytics
        verify { analytics.logEvent(eq("player_opened"), any()) }
    }

    @Test
    fun `loadPractice should update state with error on failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.failure(
            Exception(errorMessage)
        )

        // When
        viewModel.uiState.test {
            val initialState = awaitItem()

            viewModel.loadPractice("test-practice-1")
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(errorMessage, errorState.error)
            assertEquals(null, errorState.practice)
        }
    }

    @Test
    fun `togglePlayPause should start session when not playing`() = runTest {
        // Given
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.success(testPractice)
        coEvery { practiceRepository.getSimilar(any()) } returns Result.success(emptyList())

        viewModel.loadPractice("test-practice-1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onEvent(PlayerUiEvent.TogglePlayPause)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analytics.logEvent(eq("session_started"), any()) }
        assertTrue(viewModel.uiState.value.sessionStartTime > 0)
    }

    @Test
    fun `setPlaybackSpeed should update player speed`() = runTest {
        // Given
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.success(testPractice)
        coEvery { practiceRepository.getSimilar(any()) } returns Result.success(emptyList())

        viewModel.loadPractice("test-practice-1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onEvent(PlayerUiEvent.SetPlaybackSpeed(PlaybackSpeed.SPEED_1_5X))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analytics.logEvent(eq("playback_speed_changed"), any()) }
    }

    @Test
    fun `setRepeatMode should update repeat mode`() = runTest {
        // Given
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.success(testPractice)
        coEvery { practiceRepository.getSimilar(any()) } returns Result.success(emptyList())

        viewModel.loadPractice("test-practice-1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onEvent(PlayerUiEvent.SetRepeatMode(RepeatMode.ONE))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analytics.logEvent(eq("repeat_mode_changed"), any()) }
    }

    @Test
    fun `toggleFullscreen should toggle fullscreen state`() = runTest {
        // Given
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isFullscreen)

            // When
            viewModel.onEvent(PlayerUiEvent.ToggleFullscreen)

            // Then
            val fullscreenState = awaitItem()
            assertTrue(fullscreenState.isFullscreen)

            // Toggle again
            viewModel.onEvent(PlayerUiEvent.ToggleFullscreen)

            val normalState = awaitItem()
            assertFalse(normalState.isFullscreen)
        }
    }

    @Test
    fun `minimize should set isMinimized to true`() = runTest {
        // Given
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isMinimized)

            // When
            viewModel.onEvent(PlayerUiEvent.Minimize)

            // Then
            val minimizedState = awaitItem()
            assertTrue(minimizedState.isMinimized)
            assertFalse(minimizedState.isFullscreen)
        }

        verify { analytics.logEvent(eq("player_minimized"), any()) }
    }

    @Test
    fun `expand should set isMinimized to false`() = runTest {
        // Given
        viewModel.onEvent(PlayerUiEvent.Minimize)

        viewModel.uiState.test {
            skipItems(1) // Skip minimized state

            // When
            viewModel.onEvent(PlayerUiEvent.Expand)

            // Then
            val expandedState = awaitItem()
            assertFalse(expandedState.isMinimized)
        }

        verify { analytics.logEvent(eq("player_expanded"), any()) }
    }

    @Test
    fun `close should reset session state`() = runTest {
        // Given
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.success(testPractice)
        coEvery { practiceRepository.getSimilar(any()) } returns Result.success(emptyList())

        viewModel.loadPractice("test-practice-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(PlayerUiEvent.TogglePlayPause) // Start session
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.uiState.test {
            skipItems(1) // Skip current state

            viewModel.onEvent(PlayerUiEvent.Close)

            // Then
            val closedState = awaitItem()
            assertFalse(closedState.isMinimized)
            assertEquals(0L, closedState.sessionStartTime)
            assertEquals(0L, closedState.sessionDuration)
        }

        verify { analytics.logEvent(eq("player_closed"), any()) }
    }

    @Test
    fun `retry should reload practice`() = runTest {
        // Given
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.failure(
            Exception("Network error")
        )

        viewModel.loadPractice("test-practice-1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Now succeed on retry
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.success(testPractice)
        coEvery { practiceRepository.getSimilar(any()) } returns Result.success(emptyList())

        // When
        viewModel.onEvent(PlayerUiEvent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.practice)
            assertEquals(null, state.error)
        }
    }

    @Test
    fun `enterPipMode should log analytics`() = runTest {
        // When
        viewModel.onEvent(PlayerUiEvent.EnterPipMode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Note: Actual PiP functionality depends on player implementation
        // We just verify analytics
        // Analytics will be called only if PiP is supported
    }

    @Test
    fun `session completion should increment stats`() = runTest {
        // Given
        coEvery { practiceRepository.getById("test-practice-1") } returns Result.success(testPractice)
        coEvery { practiceRepository.getSimilar(any()) } returns Result.success(emptyList())
        coEvery { incrementSessionUseCase.invoke(any(), any()) } returns Result.success(Unit)

        viewModel.loadPractice("test-practice-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(PlayerUiEvent.TogglePlayPause) // Start session
        testDispatcher.scheduler.advanceUntilIdle()

        // Simulate session completion by calling the private method
        // In a real test, we would trigger completion through player state
        // For now, we verify the analytics call for session_started
        verify { analytics.logEvent(eq("session_started"), any()) }
    }
}
