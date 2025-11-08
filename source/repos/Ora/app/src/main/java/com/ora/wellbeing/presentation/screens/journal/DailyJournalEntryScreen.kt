package com.ora.wellbeing.presentation.screens.journal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.data.model.MoodType
import com.ora.wellbeing.presentation.theme.GratitudeMint
import com.ora.wellbeing.presentation.theme.GratitudePeach
import com.ora.wellbeing.presentation.theme.GratitudePink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyJournalEntryScreen(
    date: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: DailyJournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load specific date if provided
    LaunchedEffect(date) {
        if (date != null) {
            viewModel.onEvent(DailyJournalUiEvent.LoadEntry(date))
        }
    }

    // Navigate back after successful save
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.clearSaveSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal du ${uiState.formattedDate}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                actions = {
                    if (uiState.isExistingEntry) {
                        IconButton(onClick = {
                            viewModel.onEvent(DailyJournalUiEvent.DeleteEntry(uiState.currentDate))
                        }) {
                            Icon(Icons.Default.Delete, "Supprimer")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler")
                    }

                    Button(
                        onClick = { viewModel.onEvent(DailyJournalUiEvent.SaveEntry) },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.isValid && !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),  // Réduire le padding horizontal pour maximiser l'espace
                verticalArrangement = Arrangement.spacedBy(16.dp)  // Réduire l'espacement entre les items
            ) {
                // Error message
                if (uiState.error != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    uiState.error ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // 1. Mood Selector
                item {
                    MoodSelectorSection(
                        selectedMood = uiState.mood,
                        onMoodSelected = { viewModel.onEvent(DailyJournalUiEvent.UpdateMood(it)) }
                    )
                }

                // 2. Short Note
                item {
                    ShortNoteSection(
                        note = uiState.shortNote,
                        onNoteChange = { viewModel.onEvent(DailyJournalUiEvent.UpdateShortNote(it)) }
                    )
                }

                // 3. Daily Story
                item {
                    DailyStorySection(
                        story = uiState.dailyStory,
                        onStoryChange = { viewModel.onEvent(DailyJournalUiEvent.UpdateDailyStory(it)) }
                    )
                }

                // 4. Gratitudes
                item {
                    GratitudesSection(
                        gratitudes = uiState.gratitudes,
                        onGratitudeChange = { index, text ->
                            viewModel.onEvent(DailyJournalUiEvent.UpdateGratitude(index, text))
                        }
                    )
                }

                // 5. Accomplishments
                item {
                    AccomplishmentsSection(
                        accomplishments = uiState.accomplishments,
                        onAccomplishmentChange = { index, text ->
                            viewModel.onEvent(DailyJournalUiEvent.UpdateAccomplishment(index, text))
                        },
                        onAddAccomplishment = {
                            viewModel.onEvent(DailyJournalUiEvent.AddAccomplishment)
                        },
                        onRemoveAccomplishment = { index ->
                            viewModel.onEvent(DailyJournalUiEvent.RemoveAccomplishment(index))
                        }
                    )
                }

                // 6. Improvements
                item {
                    ImprovementsSection(
                        improvements = uiState.improvements,
                        onImprovementChange = { index, text ->
                            viewModel.onEvent(DailyJournalUiEvent.UpdateImprovement(index, text))
                        }
                    )
                }

                // 7. Learnings
                item {
                    LearningsSection(
                        learnings = uiState.learnings,
                        remindMeTomorrow = uiState.remindMeTomorrow,
                        onLearningsChange = {
                            viewModel.onEvent(DailyJournalUiEvent.UpdateLearnings(it))
                        },
                        onRemindMeTomorrowChange = {
                            viewModel.onEvent(DailyJournalUiEvent.UpdateRemindMeTomorrow(it))
                        }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MoodSelectorSection(
    selectedMood: String,
    onMoodSelected: (String) -> Unit
) {
    JournalSectionCard(
        title = "Humeur du jour",
        icon = Icons.Default.Mood
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoodType.values().forEach { moodType ->
                MoodButton(
                    mood = moodType,
                    isSelected = selectedMood == moodType.value,
                    onClick = { onMoodSelected(moodType.value) }
                )
            }
        }
    }
}

@Composable
private fun MoodButton(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = mood.emoji,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = mood.label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ShortNoteSection(
    note: String,
    onNoteChange: (String) -> Unit
) {
    JournalSectionCard(
        title = "Note quelques mots",
        subtitle = "${note.length}/200",
        icon = Icons.Default.Notes
    ) {
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Résumé rapide de ta journée...") },
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors()
        )
    }
}

@Composable
private fun DailyStorySection(
    story: String,
    onStoryChange: (String) -> Unit
) {
    JournalSectionCard(
        title = "Qu'as-tu envie de raconter aujourd'hui?",
        subtitle = "${story.length}/2000",
        icon = Icons.Default.AutoStories
    ) {
        OutlinedTextField(
            value = story,
            onValueChange = onStoryChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            placeholder = { Text("Raconte ta journée en détail...") },
            maxLines = 10,
            colors = OutlinedTextFieldDefaults.colors()
        )
    }
}

@Composable
private fun GratitudesSection(
    gratitudes: List<String>,
    onGratitudeChange: (Int, String) -> Unit
) {
    JournalSectionCard(
        title = "Aujourd'hui, je suis reconnaissant(e) pour...",
        icon = Icons.Default.Favorite
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            gratitudes.take(3).forEachIndexed { index, gratitude ->
                GratitudeField(
                    index = index,
                    text = gratitude,
                    onTextChange = { onGratitudeChange(index, it) }
                )
            }
        }
    }
}

@Composable
private fun GratitudeField(
    index: Int,
    text: String,
    onTextChange: (String) -> Unit
) {
    val backgroundColor = when (index) {
        0 -> GratitudePink
        1 -> GratitudePeach
        else -> GratitudeMint
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${index + 1}.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ta gratitude...") },
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                focusedContainerColor = Color.White.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun AccomplishmentsSection(
    accomplishments: List<String>,
    onAccomplishmentChange: (Int, String) -> Unit,
    onAddAccomplishment: () -> Unit,
    onRemoveAccomplishment: (Int) -> Unit
) {
    JournalSectionCard(
        title = "Aujourd'hui, j'ai accompli...",
        icon = Icons.Default.CheckCircle
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            accomplishments.forEachIndexed { index, accomplishment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = accomplishment,
                        onValueChange = { onAccomplishmentChange(index, it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Un accomplissement...") },
                        maxLines = 2,
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    if (accomplishments.size > 1) {
                        IconButton(onClick = { onRemoveAccomplishment(index) }) {
                            Icon(Icons.Default.RemoveCircle, "Supprimer")
                        }
                    }
                }
            }

            TextButton(
                onClick = onAddAccomplishment,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Ajouter un accomplissement")
            }
        }
    }
}

@Composable
private fun ImprovementsSection(
    improvements: List<String>,
    onImprovementChange: (Int, String) -> Unit
) {
    JournalSectionCard(
        title = "Qu'est-ce que je peux améliorer pour demain?",
        icon = Icons.Default.TrendingUp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            improvements.take(3).forEachIndexed { index, improvement ->
                OutlinedTextField(
                    value = improvement,
                    onValueChange = { onImprovementChange(index, it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Un point d'amélioration...") },
                    maxLines = 2,
                    leadingIcon = {
                        Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.secondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }
        }
    }
}

@Composable
private fun LearningsSection(
    learnings: String,
    remindMeTomorrow: Boolean,
    onLearningsChange: (String) -> Unit,
    onRemindMeTomorrowChange: (Boolean) -> Unit
) {
    JournalSectionCard(
        title = "Qu'est-ce que j'ai appris aujourd'hui?",
        subtitle = "${learnings.length}/1000",
        icon = Icons.Default.School
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = learnings,
                onValueChange = onLearningsChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { Text("Tes apprentissages du jour...") },
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRemindMeTomorrowChange(!remindMeTomorrow) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = remindMeTomorrow,
                    onCheckedChange = onRemindMeTomorrowChange
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Me le rappeler demain",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun JournalSectionCard(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}
