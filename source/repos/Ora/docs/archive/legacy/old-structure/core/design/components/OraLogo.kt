package core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.design.OraColors
import core.design.OraTheme

@Composable
fun OraLogo(
    modifier: Modifier = Modifier,
    showSubtitle: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo avec soleil
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(OraColors.OraOrange),
            contentAlignment = Alignment.Center
        ) {
            // Rayons du soleil en arrière-plan
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Texte ORA
        Text(
            text = "ORA",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            color = OraColors.OraOrange
        )

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(4.dp))

            // Sous-titre
            Text(
                text = "Body • Mind • Soul",
                style = MaterialTheme.typography.bodyMedium,
                color = OraColors.OnSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OraLogoPreview() {
    OraTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.OraBeige)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            OraLogo()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OraLogoNoSubtitlePreview() {
    OraTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.OraBeige)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            OraLogo(showSubtitle = false)
        }
    }
}