package com.ora.wellbeing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
// FIX(build-debug-android): Correction du package d'import - utilisation de presentation.navigation au lieu de ui.navigation
import com.ora.wellbeing.presentation.navigation.OraNavigation
import com.ora.wellbeing.presentation.theme.OraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Configuration du splash screen
        installSplashScreen()

        // Configuration edge-to-edge
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            OraApp()
        }
    }
}

@Composable
fun OraApp() {
    OraTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            OraNavigation()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OraAppPreview() {
    OraApp()
}