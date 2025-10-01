package com.ora.wellbeing.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ora.wellbeing.domain.model.Mood
import com.ora.wellbeing.ui.components.MoodSelector
import com.ora.wellbeing.ui.theme.OraColors

@Composable
fun JournalDetailScreen(
    onBackClick: () -> Unit
) {
    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var storyText by remember { mutableStateOf("") }
    var gratitude1 by remember { mutableStateOf("") }
    var gratitude2 by remember { mutableStateOf("") }
    var gratitude3 by remember { mutableStateOf("") }
    var accomplishments by remember { mutableStateOf("") }
    var improvements by remember { mutableStateOf("") }
    var learnings by remember { mutableStateOf("") }
    var hasLearned by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OraColors.Background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Journal Quotidien",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OraColors.TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = OraColors.Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OraColors.Background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section Mood
            Text(
                text = "Comment tu te sens aujourd'hui?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OraColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            MoodSelector(
                selectedMood = selectedMood,
                onMoodSelected = { selectedMood = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Section Histoire
            Text(
                text = "Qu'as-tu envie de raconter aujourd'hui?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OraColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = storyText,
                onValueChange = { storyText = it },
                placeholder = {
                    Text(
                        text = "Raconte ta journée...",
                        color = OraColors.TextLight
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OraColors.Primary,
                    unfocusedBorderColor = OraColors.TextLight.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Section Gratitudes
            Text(
                text = "Aujourd'hui, je suis reconnaissant(e) pour...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OraColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = gratitude1,
                onValueChange = { gratitude1 = it },
                placeholder = {
                    Text(
                        text = "1. Ma première gratitude",
                        color = OraColors.TextLight
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OraColors.Primary,
                    unfocusedBorderColor = OraColors.TextLight.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = gratitude2,
                onValueChange = { gratitude2 = it },
                placeholder = {
                    Text(
                        text = "2. Ma deuxième gratitude",
                        color = OraColors.TextLight
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OraColors.Primary,
                    unfocusedBorderColor = OraColors.TextLight.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = gratitude3,
                onValueChange = { gratitude3 = it },
                placeholder = {
                    Text(
                        text = "3. Ma troisième gratitude",
                        color = OraColors.TextLight
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OraColors.Primary,
                    unfocusedBorderColor = OraColors.TextLight.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Cards vertes pour accomplissements et améliorations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card Accomplissements
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = OraColors.YogaGreen.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Mes accomplissements",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OraColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = accomplishments,
                            onValueChange = { accomplishments = it },
                            placeholder = {
                                Text(
                                    text = "Ce dont je suis fier...",
                                    fontSize = 12.sp,
                                    color = OraColors.TextLight
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OraColors.YogaGreen,
                                unfocusedBorderColor = OraColors.TextLight.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Card Améliorations
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = OraColors.YogaGreen.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "À améliorer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OraColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = improvements,
                            onValueChange = { improvements = it },
                            placeholder = {
                                Text(
                                    text = "Ce que je peux mieux faire...",
                                    fontSize = 12.sp,
                                    color = OraColors.TextLight
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OraColors.YogaGreen,
                                unfocusedBorderColor = OraColors.TextLight.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Section Apprentissages
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasLearned,
                    onCheckedChange = { hasLearned = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = OraColors.Primary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Qu'est-ce que j'ai appris aujourd'hui?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OraColors.TextPrimary
                )
            }

            if (hasLearned) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = learnings,
                    onValueChange = { learnings = it },
                    placeholder = {
                        Text(
                            text = "Décris ce que tu as appris...",
                            color = OraColors.TextLight
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OraColors.Primary,
                        unfocusedBorderColor = OraColors.TextLight.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bouton Sauvegarder
            Button(
                onClick = {
                    // Sauvegarder les données
                    onBackClick()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OraColors.Primary
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Sauvegarder",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Sauvegarder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}