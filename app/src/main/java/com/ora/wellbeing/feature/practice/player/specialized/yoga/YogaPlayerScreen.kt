package com.ora.wellbeing.feature.practice.player.specialized.yoga

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
    onBack: () -> Unit,
    onMinimize: () -> Unit
) {
    val practice = uiState.practice ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = practice.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (uiState.currentSide != YogaSide.NONE) {
                        Text(
                            text = stringResource(uiState.currentSide.nameRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = PlayerColors.Yoga.accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.player_back))
                }
            },
            actions = {
                // Bouton mode miroir
                IconButton(onClick = { onEvent(YogaPlayerEvent.ToggleMirrorMode) }) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = stringResource(R.string.yoga_mirror_mode),
                        tint = if (uiState.isMirrorMode) PlayerColors.Yoga.accent else Color.Gray
                    )
                }
                IconButton(onClick = { onEvent(YogaPlayerEvent.ToggleFullscreen) }) {
                    Icon(
                        if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        stringResource(R.string.yoga_fullscreen)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PlayerColors.Yoga.background
            )
        )

        // Video Player avec mode miroir
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (uiState.isFullscreen) 1f else 0.45f)
                .clip(RoundedCornerShape(if (uiState.isFullscreen) 0.dp else 16.dp))
                .background(Color.Black)
                .graphicsLayer {
                    // Appliquer le mode miroir
                    scaleX = if (uiState.isMirrorMode) -1f else 1f
                }
        ) {
            val context = LocalContext.current
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
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

            // Buffering indicator
            if (uiState.playerState.buffering) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }

        // Contrôles et infos (hors fullscreen)
        if (!uiState.isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chapitre actuel
                if (uiState.chapters.isNotEmpty()) {
                    CurrentChapterBadge(
                        chapterTitle = uiState.chapters.getOrNull(uiState.currentChapterIndex)?.title ?: "",
                        chapterIndex = uiState.currentChapterIndex,
                        totalChapters = uiState.chapters.size
                    )
                }

                // Aperçu prochaine posture
                AnimatedVisibility(
                    visible = uiState.nextPosePreview != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    uiState.nextPosePreview?.let { preview ->
                        NextPosePreviewCard(preview = preview)
                    }
                }

                // Seek bar
                CustomSeekBar(
                    currentPosition = uiState.playerState.currentPosition,
                    duration = uiState.playerState.duration,
                    onSeek = { onEvent(YogaPlayerEvent.SeekTo(it)) }
                )

                // Chapitres en barre horizontale
                ChapterBar(
                    chapters = uiState.chapters,
                    currentIndex = uiState.currentChapterIndex,
                    onChapterSelected = { onEvent(YogaPlayerEvent.GoToChapter(it)) }
                )

                // Contrôles principaux
                YogaMainControls(
                    isPlaying = uiState.playerState.isPlaying,
                    currentSide = uiState.currentSide,
                    onPlayPause = { onEvent(YogaPlayerEvent.TogglePlayPause) },
                    onSeekBack = { onEvent(YogaPlayerEvent.SeekBackward) },
                    onSeekForward = { onEvent(YogaPlayerEvent.SeekForward) },
                    onSwitchSide = { onEvent(YogaPlayerEvent.SwitchSide) },
                    onPreviousChapter = { onEvent(YogaPlayerEvent.PreviousChapter) },
                    onNextChapter = { onEvent(YogaPlayerEvent.NextChapter) }
                )
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
