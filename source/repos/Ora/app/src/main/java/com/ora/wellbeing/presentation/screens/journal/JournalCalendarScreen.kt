package com.ora.wellbeing.presentation.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.data.model.DailyJournalEntry
import com.ora.wellbeing.domain.repository.DailyJournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalCalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (String) -> Unit,
    viewModel: JournalCalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendrier du journal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Month selector
            MonthSelector(
                currentMonth = uiState.currentMonth,
                onPreviousMonth = { viewModel.onEvent(CalendarUiEvent.PreviousMonth) },
                onNextMonth = { viewModel.onEvent(CalendarUiEvent.NextMonth) }
            )

            Spacer(Modifier.height(16.dp))

            // Calendar grid
            CalendarGrid(
                currentMonth = uiState.currentMonth,
                entries = uiState.entriesMap,
                onDateClick = { date ->
                    onNavigateToEntry(date)
                }
            )

            Spacer(Modifier.height(16.dp))

            // Statistics
            MonthStatistics(
                totalEntries = uiState.entriesMap.size,
                currentMonth = uiState.currentMonth
            )
        }
    }
}

@Composable
private fun MonthSelector(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ArrowBackIosNew, "Mois précédent")
            }

            Text(
                text = currentMonth.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)
                ).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onNextMonth,
                enabled = currentMonth < YearMonth.now()
            ) {
                Icon(Icons.Default.ArrowForwardIos, "Mois suivant")
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    entries: Map<String, DailyJournalEntry>,
    onDateClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar days
            val firstDayOfMonth = currentMonth.atDay(1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday
            val daysInMonth = currentMonth.lengthOfMonth()
            val totalCells = firstDayOfWeek - 1 + daysInMonth

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                // Empty cells before first day
                items(firstDayOfWeek - 1) {
                    Box(modifier = Modifier.size(40.dp))
                }

                // Days of month
                items(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val date = currentMonth.atDay(day)
                    val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val entry = entries[dateString]
                    val isToday = date == LocalDate.now()

                    DayCell(
                        day = day,
                        entry = entry,
                        isToday = isToday,
                        onClick = { onDateClick(dateString) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    entry: DailyJournalEntry?,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        entry != null -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }

    val borderColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (entry != null) FontWeight.Bold else FontWeight.Normal,
                color = if (entry != null) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            if (entry != null) {
                Text(
                    text = entry.getMoodEmoji(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MonthStatistics(
    totalEntries: Int,
    currentMonth: YearMonth
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = totalEntries.toString(),
                label = "Entrées ce mois"
            )

            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )

            StatItem(
                value = "${currentMonth.lengthOfMonth()}",
                label = "Jours au total"
            )

            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )

            StatItem(
                value = if (currentMonth.lengthOfMonth() > 0)
                    "${(totalEntries * 100 / currentMonth.lengthOfMonth())}%"
                else "0%",
                label = "Complétude"
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ViewModel for Calendar Screen
@HiltViewModel
class JournalCalendarViewModel @Inject constructor(
    private val dailyJournalRepository: DailyJournalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMonth()
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            CalendarUiEvent.PreviousMonth -> {
                val newMonth = _uiState.value.currentMonth.minusMonths(1)
                _uiState.value = _uiState.value.copy(currentMonth = newMonth)
                loadMonth(newMonth)
            }
            CalendarUiEvent.NextMonth -> {
                val newMonth = _uiState.value.currentMonth.plusMonths(1)
                if (newMonth <= YearMonth.now()) {
                    _uiState.value = _uiState.value.copy(currentMonth = newMonth)
                    loadMonth(newMonth)
                }
            }
        }
    }

    private fun loadCurrentMonth() {
        loadMonth(YearMonth.now())
    }

    private fun loadMonth(yearMonth: YearMonth) {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("loadMonth: No authenticated user")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val yearMonthString = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

                dailyJournalRepository.observeEntriesForMonth(uid, yearMonthString)
                    .collect { entries ->
                        val entriesMap = entries.associateBy { it.date }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            entriesMap = entriesMap
                        )
                        Timber.d("loadMonth: Loaded ${entries.size} entries for $yearMonthString")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur: ${e.message}"
                )
                Timber.e(e, "loadMonth: Error loading month")
            }
        }
    }
}

data class CalendarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentMonth: YearMonth = YearMonth.now(),
    val entriesMap: Map<String, DailyJournalEntry> = emptyMap()
)

sealed interface CalendarUiEvent {
    data object PreviousMonth : CalendarUiEvent
    data object NextMonth : CalendarUiEvent
}
