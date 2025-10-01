package com.ora.wellbeing.ui.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ora.wellbeing.ui.navigation.bottomNavigationItems
import com.ora.wellbeing.ui.theme.OraColors

@Composable
fun OraBottomNavigation(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = OraColors.Background,
        contentColor = OraColors.Primary,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        bottomNavigationItems.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) OraColors.Primary else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon!!,
                            contentDescription = screen.title,
                            tint = if (isSelected) Color.White else OraColors.TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) OraColors.Primary else OraColors.TextSecondary
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = OraColors.TextSecondary,
                    selectedTextColor = OraColors.Primary,
                    unselectedTextColor = OraColors.TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}