package com.ora.wellbeing.presentation.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ora.wellbeing.R
import kotlinx.coroutines.delay

/**
 * Celebration Screen shown after completing onboarding
 * Features animations and encouraging message
 */
@Composable
fun OnboardingCelebrationScreen(
    onContinue: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    // Trigger animations on first composition
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    // Animated scale for celebration emoji
    val infiniteTransition = rememberInfiniteTransition(label = "celebration_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated celebration icon
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .scale(if (isVisible) scale else 0f),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.scale(1.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically()
            ) {
                Text(
                    text = stringResource(R.string.onboarding_celebration_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = tween(durationMillis = 600, delayMillis = 200)
                ) + androidx.compose.animation.slideInVertically(
                    animationSpec = tween(durationMillis = 600, delayMillis = 200)
                )
            ) {
                Text(
                    text = stringResource(R.string.onboarding_celebration_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Features cards
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = tween(durationMillis = 600, delayMillis = 400)
                ) + androidx.compose.animation.slideInVertically(
                    animationSpec = tween(durationMillis = 600, delayMillis = 400)
                )
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        emoji = "✨",
                        title = stringResource(R.string.onboarding_feature_personalized_title),
                        description = stringResource(R.string.onboarding_feature_personalized_desc)
                    )
                    FeatureCard(
                        emoji = "📊",
                        title = stringResource(R.string.onboarding_feature_tracking_title),
                        description = stringResource(R.string.onboarding_feature_tracking_desc)
                    )
                    FeatureCard(
                        emoji = "🏆",
                        title = stringResource(R.string.onboarding_feature_programs_title),
                        description = stringResource(R.string.onboarding_feature_programs_desc)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Continue button
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = tween(durationMillis = 600, delayMillis = 600)
                ) + androidx.compose.animation.scaleIn(
                    animationSpec = tween(durationMillis = 600, delayMillis = 600)
                )
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_start_journey),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    emoji: String,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emoji
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}
