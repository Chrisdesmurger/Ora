package com.ora.wellbeing.feature.practice.player.specialized.yoga

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.ora.wellbeing.R
import com.ora.wellbeing.core.util.ForceLandscapeOrientation
import com.ora.wellbeing.core.util.ImmersiveModeEffect
import com.ora.wellbeing.core.util.KeepScreenOn
import com.ora.wellbeing.feature.practice.player.specialized.PlayerColors
import com.ora.wellbeing.feature.practice.ui.CustomSeekBar

/**
 * Écran lecteur spécialisé pour Yoga/Pilates
 * - Mode miroir pour suivre plus facilement
 * - Indicateur de côté (Gauche/Droit)
 * - Chapitres par posture
 * - Aperçu de la prochaine posture
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YogaPlayerScreen(
    practiceId: String,
    onBack: () -> Unit,
    onMinimize: () -> Unit,
    viewModel: YogaPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Force landscape orientation for yoga video playback
    ForceLandscapeOrientation()

    // Keep screen on during playback
    KeepScreenOn()

    // Enable immersive mode when in fullscreen
    ImmersiveModeEffect(enabled = uiState.isFullscreen)

    LaunchedEffect(practiceId) {
        viewModel.loadPractice(practiceId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerColors.Yoga.background)
    ) {
        when {
            uiState.isLoading -> YogaLoadingState()
            uiState.error != null -> YogaErrorState(
                error = uiState.error ?: "",
                onRetry = { viewModel.onEvent(YogaPlayerEvent.Retry) },
                onBack = onBack
            )
            uiState.practice != null -> YogaPlayerContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                exoPlayer = viewModel.getExoPlayer(),
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
    }
}

@Composable
private fun YogaLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = PlayerColors.Yoga.accent)
            Text(
                text = stringResource(R.string.yoga_preparing),
                style = MaterialTheme.typography.bodyLarge,
                color = PlayerColors.Yoga.onBackground
            )
        }
    }
}

@Composable
private fun YogaErrorState(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) { Text(stringResource(R.string.player_back)) }
                Button(onClick = onRetry) { Text(stringResource(R.string.player_retry)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YogaPlayerContent(
    uiState: YogaPlayerState,
    onEvent: (YogaPlayerEvent) -> Unit,
    exoPlayer: androidx.media3.exoplayer.ExoPlayer?,
    onBack: () -> Unit,
    onMinimize: () -> Unit
) {
    val practice = uiState.practice ?: return

    // Landscape layout: Video (70%) | Controls (30%)
    Row(modifier = Modifier.fillMaxSize()) {
        // Left side: Video Player with overlaid seek bar
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .background(Color.Black)
                .graphicsLayer {
                    // Appliquer le mode miroir
                    scaleX = if (uiState.isMirrorMode) -1f else 1f
                }
                .clickable { onEvent(YogaPlayerEvent.TogglePlayPause) }
        ) {
            // Video PlayerView connected to ExoPlayer
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                        player = exoPlayer
                    }
                },
                update = { playerView ->
                    playerView.player = exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )

            // Indicateur mode miroir
            if (uiState.isMirrorMode) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .graphicsLayer { scaleX = -1f }, // Réinverser le texte
                    shape = RoundedCornerShape(8.dp),
                    color = PlayerColors.Yoga.accent.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Flip,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            text = stringResource(R.string.yoga_mirror_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Side indicator overlay
            if (uiState.currentSide != YogaSide.NONE) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .graphicsLayer { scaleX = if (uiState.isMirrorMode) -1f else 1f },
                    shape = RoundedCornerShape(8.dp),
                    color = PlayerColors.Yoga.accentDark.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = stringResource(uiState.currentSide.nameRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Fullscreen toggle button (top-right, after side indicator)
            IconButton(
                onClick = { onEvent(YogaPlayerEvent.ToggleFullscreen) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = if (uiState.currentSide != YogaSide.NONE) 48.dp else 8.dp, end = 8.dp)
                    .graphicsLayer { scaleX = if (uiState.isMirrorMode) -1f else 1f }
            ) {
                Icon(
                    if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = stringResource(
                        if (uiState.isFullscreen) R.string.yoga_exit_fullscreen
                        else R.string.yoga_enter_fullscreen
                    ),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Buffering indicator
            if (uiState.playerState.buffering) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            // Play/Pause overlay (only visible when paused)
            if (!uiState.playerState.isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.player_play),
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }

            // Seek bar overlay at the bottom of the video (transparent background)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .graphicsLayer { scaleX = if (uiState.isMirrorMode) -1f else 1f }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                CustomSeekBar(
                    currentPosition = uiState.playerState.currentPosition,
                    duration = uiState.playerState.duration,
                    onSeek = { onEvent(YogaPlayerEvent.SeekTo(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Right side: Controls Panel (30%)
        Column(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .background(PlayerColors.Yoga.background)
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section: Back button + Title
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.player_back),
                        tint = PlayerColors.Yoga.onBackground
                    )
                }

                // Title
                Text(
                    text = practice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = PlayerColors.Yoga.onBackground
                )

                // Current chapter badge
                if (uiState.chapters.isNotEmpty()) {
                    CurrentChapterBadge(
                        chapterTitle = uiState.chapters.getOrNull(uiState.currentChapterIndex)?.title ?: "",
                        chapterIndex = uiState.currentChapterIndex,
                        totalChapters = uiState.chapters.size
                    )
                }

                // Next pose preview
                AnimatedVisibility(
                    visible = uiState.nextPosePreview != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    uiState.nextPosePreview?.let { preview ->
                        NextPosePreviewCard(preview = preview)
                    }
                }
            }

            // Middle section: Chapter list (scrollable)
            if (uiState.chapters.isNotEmpty()) {
                ChapterList(
                    chapters = uiState.chapters,
                    currentIndex = uiState.currentChapterIndex,
                    onChapterSelected = { onEvent(YogaPlayerEvent.GoToChapter(it)) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Bottom section: Controls
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onEvent(YogaPlayerEvent.PreviousChapter) }) {
                        Icon(Icons.Default.SkipPrevious, stringResource(R.string.yoga_previous_pose))
                    }
                    IconButton(onClick = { onEvent(YogaPlayerEvent.SeekBackward) }) {
                        Icon(Icons.Default.Replay10, stringResource(R.string.yoga_rewind_10s))
                    }
                    IconButton(onClick = { onEvent(YogaPlayerEvent.SeekForward) }) {
                        Icon(Icons.Default.Forward10, stringResource(R.string.yoga_forward_10s))
                    }
                    IconButton(onClick = { onEvent(YogaPlayerEvent.NextChapter) }) {
                        Icon(Icons.Default.SkipNext, stringResource(R.string.yoga_next_pose_button))
                    }
                }

                // Mirror mode toggle
                OutlinedButton(
                    onClick = { onEvent(YogaPlayerEvent.ToggleMirrorMode) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (uiState.isMirrorMode) PlayerColors.Yoga.accent else PlayerColors.Yoga.onBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Flip,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.yoga_mirror_mode))
                }

                // Switch side button (when applicable)
                if (uiState.currentSide != YogaSide.NONE) {
                    Button(
                        onClick = { onEvent(YogaPlayerEvent.SwitchSide) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PlayerColors.Yoga.accent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.yoga_switch_side))
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentChapterBadge(
    chapterTitle: String,
    chapterIndex: Int,
    totalChapters: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PlayerColors.Yoga.accent.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = PlayerColors.Yoga.accent
            )
            Column {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PlayerColors.Yoga.accentDark
                )
                Text(
                    text = stringResource(R.string.yoga_pose_number, chapterIndex + 1, totalChapters),
                    style = MaterialTheme.typography.bodySmall,
                    color = PlayerColors.Yoga.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun NextPosePreviewCard(preview: PosePreview) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PlayerColors.Yoga.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = null,
                tint = PlayerColors.Yoga.accent,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.yoga_next_pose),
                    style = MaterialTheme.typography.labelSmall,
                    color = PlayerColors.Yoga.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = preview.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Surface(
                shape = CircleShape,
                color = PlayerColors.Yoga.accent.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${preview.timeUntil / 1000}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = PlayerColors.Yoga.accent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ChapterBar(
    chapters: List<com.ora.wellbeing.feature.practice.ui.Chapter>,
    currentIndex: Int,
    onChapterSelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        itemsIndexed(chapters) { index, chapter ->
            val isActive = index == currentIndex
            val isPast = index < currentIndex

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when {
                    isActive -> PlayerColors.Yoga.accent
                    isPast -> PlayerColors.Yoga.chapterActive.copy(alpha = 0.3f)
                    else -> PlayerColors.Yoga.chapterInactive
                },
                modifier = Modifier.clickable { onChapterSelected(index) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPast) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PlayerColors.Yoga.accentDark
                        )
                    }
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) Color.White else PlayerColors.Yoga.onBackground,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Vertical chapter list for landscape mode controls panel.
 * Shows all chapters with visual indicators for current, past, and future states.
 */
@Composable
private fun ChapterList(
    chapters: List<com.ora.wellbeing.feature.practice.ui.Chapter>,
    currentIndex: Int,
    onChapterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(chapters) { index, chapter ->
            val isActive = index == currentIndex
            val isPast = index < currentIndex

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isActive -> PlayerColors.Yoga.accent
                    isPast -> PlayerColors.Yoga.chapterActive.copy(alpha = 0.2f)
                    else -> PlayerColors.Yoga.chapterInactive.copy(alpha = 0.5f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChapterSelected(index) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chapter number or check mark
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color.White.copy(alpha = 0.3f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPast) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = PlayerColors.Yoga.accentDark
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color.White else PlayerColors.Yoga.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Chapter title
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) Color.White else PlayerColors.Yoga.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun YogaMainControls(
    isPlaying: Boolean,
    currentSide: YogaSide,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSwitchSide: () -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Contrôles principaux
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousChapter) {
                Icon(Icons.Default.SkipPrevious, stringResource(R.string.yoga_previous_pose), modifier = Modifier.size(28.dp))
            }
            IconButton(onClick = onSeekBack) {
                Icon(Icons.Default.Replay10, stringResource(R.string.yoga_rewind_10s), modifier = Modifier.size(32.dp))
            }
            FloatingActionButton(
                onClick = onPlayPause,
                containerColor = PlayerColors.Yoga.accent,
                contentColor = Color.White
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play),
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = onSeekForward) {
                Icon(Icons.Default.Forward10, stringResource(R.string.yoga_forward_10s), modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = onNextChapter) {
                Icon(Icons.Default.SkipNext, stringResource(R.string.yoga_next_pose_button), modifier = Modifier.size(28.dp))
            }
        }

        // Bouton changement de côté
        if (currentSide != YogaSide.NONE) {
            OutlinedButton(
                onClick = onSwitchSide,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PlayerColors.Yoga.accent
                )
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.yoga_switch_side))
            }
        }
    }
}
