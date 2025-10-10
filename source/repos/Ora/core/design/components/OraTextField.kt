package core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme

@Composable
fun OraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    leadingIcon: ImageVector? = null,
    backgroundColor: Color = OraColors.OraBeige,
    textColor: Color = OraColors.OnSurface,
    placeholderColor: Color = OraColors.OnSurfaceVariant,
    minHeight: Int = 56,
    maxLines: Int = 1,
    singleLine: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label optionnel
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }

        // Champ de texte
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = if (maxLines == 1) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icône de début optionnelle
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = placeholderColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Champ de texte
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (maxLines > 1) Modifier.heightIn(min = (minHeight - 32).dp)
                            else Modifier
                        ),
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight
                    ),
                    maxLines = if (singleLine) 1 else maxLines,
                    singleLine = singleLine,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (maxLines == 1) Alignment.CenterStart else Alignment.TopStart
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = placeholderColor
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OraJournalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String = "Écris tes pensées ici..."
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Titre de la section
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = OraColors.OnSurface,
                fontWeight = FontWeight.SemiBold
            )

            // Zone de texte
            OraTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                backgroundColor = OraColors.OraBeige.copy(alpha = 0.3f),
                minHeight = 120,
                maxLines = 5,
                singleLine = false,
                leadingIcon = Icons.Default.Edit
            )
        }
    }
}

@Composable
fun OraQuickNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Note rapide..."
) {
    OraTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        backgroundColor = Color.White,
        minHeight = 48
    )
}

@Preview(showBackground = true)
@Composable
fun OraTextFieldPreview() {
    OraTheme {
        var text1 by remember { mutableStateOf("") }
        var text2 by remember { mutableStateOf("") }
        var journalText by remember { mutableStateOf("") }
        var quickNote by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Champ simple avec label
            OraTextField(
                value = text1,
                onValueChange = { text1 = it },
                label = "Nom d'utilisateur",
                placeholder = "Entrez votre nom",
                leadingIcon = Icons.Default.Edit
            )

            // Champ simple sans label
            OraTextField(
                value = text2,
                onValueChange = { text2 = it },
                placeholder = "Comment te sens-tu aujourd'hui..."
            )

            // Champ journal
            OraJournalTextField(
                value = journalText,
                onValueChange = { journalText = it },
                title = "Journal du jour",
                placeholder = "Écris comment tu te sens aujourd'hui..."
            )

            // Note rapide
            OraQuickNoteField(
                value = quickNote,
                onValueChange = { quickNote = it }
            )
        }
    }
}