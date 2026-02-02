package com.ora.wellbeing.presentation.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas // FIX(build-debug-android): Import manquant pour accès au Canvas natif
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.presentation.theme.OraTheme

// Couleurs spécifiques selon mockup
private val StatsBackgroundColor = Color(0xFFFAF7F2)
private val CardBackgroundColor = Color(0xFFFFFBF8)
private val OraOrange = Color(0xFFF4845F)
private val OraOrangeLight = Color(0xFFFDB5A0)
private val TextDark = Color(0xFF2C2C2C)
private val TextMedium = Color(0xFF6B6B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeStatsScreen(
    onNavigateBack: () -> Unit = {},
    onStartNewSession: () -> Unit = {},
    viewModel: PracticeStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mes stats : ${uiState.practiceDetails?.name ?: ""}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 32.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(PracticeStatsUiEvent.NavigateBack)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StatsBackgroundColor
                )
            )
        },
        containerColor = StatsBackgroundColor
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OraOrange)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Erreur inconnue",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                PracticeStatsContent(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    onStartNewSession = {
                        viewModel.onEvent(PracticeStatsUiEvent.StartNewSession)
                        onStartNewSession()
                    }
                )
            }
        }
    }
}

@Composable
private fun PracticeStatsContent(
    modifier: Modifier = Modifier,
    uiState: PracticeStatsUiState,
    onStartNewSession: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StatsBackgroundColor)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Section: 3 cartes de stats (Total, Régularité, Progrès)
        item {
            uiState.practiceDetails?.let { details ->
                StatsCardsRow(details = details)
            }
        }

        // Section: Graphique "Minutes par semaine"
        item {
            WeeklyChart(
                weeklyData = uiState.weeklyStats,
                title = "Minutes par semaine"
            )
        }

        // Bouton "Get started"
        item {
            Button(
                onClick = onStartNewSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OraOrange
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Get started",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = Color.White
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatsCardsRow(details: PracticeDetails) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Total",
            value = details.totalTime
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Régularité",
            value = "${details.regularityDays} jours"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Progrès",
            value = "${details.sessionsCount} séances"
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMedium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun WeeklyChart(
    weeklyData: List<WeeklyDataPoint>,
    title: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (weeklyData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune donnée disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMedium
                    )
                }
            } else {
                LineChart(
                    data = weeklyData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    data: List<WeeklyDataPoint>,
    modifier: Modifier = Modifier
) {
    val maxMinutes = data.maxOfOrNull { it.minutes } ?: 60
    val chartColor = OraOrange

    Canvas(modifier = modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size + 1)

        // Dessiner les lignes horizontales de grille (15, 30, 45, 60)
        val gridColor = Color.LightGray.copy(alpha = 0.3f)
        val gridLevels = listOf(15, 30, 45, 60)
        gridLevels.forEach { level ->
            val y = height - (level.toFloat() / maxMinutes) * height
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Créer les points du graphique
        val points = data.mapIndexed { index, dataPoint ->
            val x = spacing * (index + 1)
            val y = height - (dataPoint.minutes.toFloat() / maxMinutes) * height
            Offset(x, y)
        }

        // Dessiner la ligne entre les points
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = path,
                color = chartColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Dessiner les points circulaires
        points.forEach { point ->
            drawCircle(
                color = chartColor,
                radius = 8.dp.toPx(),
                center = point
            )
        }

        // FIX(build-debug-android): Utilisation correcte de drawContext.canvas.nativeCanvas pour accéder au Canvas natif Android
        // Dessiner les labels des jours (L, M, M, J, V, S, D)
        data.forEachIndexed { index, dataPoint ->
            val x = spacing * (index + 1)
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    dataPoint.dayLabel,
                    x,
                    height + 40f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#6B6B6B")
                        textSize = 36f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        // Dessiner les valeurs Y (15, 30, 45, 60)
        gridLevels.forEach { level ->
            val y = height - (level.toFloat() / maxMinutes) * height
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    level.toString(),
                    -20f,
                    y + 12f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#6B6B6B")
                        textSize = 32f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
        }
    }
}

// PREVIEW
@Preview(showBackground = true)
@Composable
fun PracticeStatsScreenPreview() {
    OraTheme {
        PracticeStatsContent(
            uiState = PracticeStatsUiState(
                isLoading = false,
                practiceDetails = PracticeDetails(
                    id = "yoga",
                    name = "Yoga",
                    totalTime = "6h 30m",
                    regularityDays = 18,
                    sessionsCount = 9,
                    monthlyTime = "3h 45 ce mois-ci",
                    growthPercentage = 20
                ),
                weeklyStats = listOf(
                    WeeklyDataPoint("L", 25, 1),
                    WeeklyDataPoint("M", 30, 2),
                    WeeklyDataPoint("M", 40, 3),
                    WeeklyDataPoint("J", 42, 4),
                    WeeklyDataPoint("V", 60, 5),
                    WeeklyDataPoint("D", 35, 7)
                ),
                practiceBreakdown = listOf(
                    PracticeTypeBreakdown("Yoga doux", 35, Color(0xFFF4845F), 140),
                    PracticeTypeBreakdown("Yoga danse", 50, Color(0xFFFDB5A0), 200),
                    PracticeTypeBreakdown("Yoga power", 15, Color(0xFF7BA089), 60)
                ),
                sessionHistory = listOf(
                    SessionHistoryItem("1", "14 avr.", "45 min", "Yoga doux du matin"),
                    SessionHistoryItem("2", "9 avr.", "20 min", "Yoga express"),
                    SessionHistoryItem("3", "2 avr.", "40 min", "Yoga flow")
                )
            ),
            onStartNewSession = {}
        )
    }
}