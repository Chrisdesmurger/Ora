package feature.profile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme
import core.design.components.*

data class ProfileMenuItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    var userName by remember { mutableStateOf("Sarah Martin") }
    var userEmail by remember { mutableStateOf("sarah.martin@email.com") }

    val profileStats = remember {
        listOf(
            Triple("Sessions", "127", OraColors.YogaGreen),
            Triple("Minutes", "2,450", OraColors.MeditationPurple),
            Triple("Jours", "45", OraColors.PilatesOrange)
        )
    }

    val menuItems = remember {
        listOf(
            ProfileMenuItem(
                title = "Mes préférences",
                subtitle = "Catégories favorites, notifications",
                icon = Icons.Default.Settings
            ),
            ProfileMenuItem(
                title = "Historique",
                subtitle = "Toutes mes sessions",
                icon = Icons.Default.History
            ),
            ProfileMenuItem(
                title = "Objectifs",
                subtitle = "Définir mes objectifs bien-être",
                icon = Icons.Default.EmojiEvents
            ),
            ProfileMenuItem(
                title = "Rappels",
                subtitle = "Notifications et rappels",
                icon = Icons.Default.Notifications
            ),
            ProfileMenuItem(
                title = "Partage",
                subtitle = "Inviter des amis",
                icon = Icons.Default.Share
            ),
            ProfileMenuItem(
                title = "Aide & Support",
                subtitle = "FAQ, nous contacter",
                icon = Icons.Default.Help
            ),
            ProfileMenuItem(
                title = "À propos",
                subtitle = "Version, mentions légales",
                icon = Icons.Default.Info
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OraColors.Background),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Header avec profil
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(OraColors.OraOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.split(" ").map { it.first() }.joinToString(""),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nom et email
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = OraColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OraColors.OnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Statistiques
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        profileStats.forEach { (label, value, color) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OraColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // Badge de progression
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = OraColors.YogaGreen.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = OraColors.YogaGreen,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Série de 7 jours !",
                            style = MaterialTheme.typography.titleMedium,
                            color = OraColors.OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Continue comme ça pour débloquer le prochain niveau",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OraColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Menu items
        items(menuItems.size) { index ->
            val item = menuItems[index]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { item.onClick() },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = OraColors.OraOrange,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = OraColors.OnSurface,
                            fontWeight = FontWeight.Medium
                        )

                        if (item.subtitle != null) {
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OraColors.OnSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = OraColors.OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            // Bouton de déconnexion
            OutlinedButton(
                onClick = { /* Logout logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, Color.Red).brush
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Se déconnecter",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun ProfileScreenPreview() {
    OraTheme {
        ProfileScreen()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProfileScreenMobilePreview() {
    OraTheme {
        ProfileScreen()
    }
}