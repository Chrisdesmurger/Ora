package core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme

enum class GratitudeCardColor(val backgroundColor: Color, val textColor: Color) {
    PINK(OraColors.GratitudePink, Color(0xFF7B1FA2)),
    ORANGE(OraColors.GratitudeOrange, Color(0xFFE65100)),
    GREEN(OraColors.GratitudeGreen, Color(0xFF2E7D32))
}

@Composable
fun OraGratitudeCard(
    text: String,
    onTextChange: (String) -> Unit,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    cardColor: GratitudeCardColor,
    modifier: Modifier = Modifier,
    placeholder: String = "Écris ce pour quoi tu es reconnaissant..."
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox
            IconButton(
                onClick = { onCheckedChange(!isChecked) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (isChecked) "Checked" else "Unchecked",
                    tint = cardColor.textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Champ de texte éditable
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textStyle = TextStyle(
                    color = cardColor.textColor,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = cardColor.textColor.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
fun OraGratitudeList(
    gratitudeItems: List<GratitudeItem>,
    onItemChange: (Int, String) -> Unit,
    onCheckedChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        itemsIndexed(gratitudeItems) { index, item ->
            OraGratitudeCard(
                text = item.text,
                onTextChange = { newText -> onItemChange(index, newText) },
                isChecked = item.isChecked,
                onCheckedChange = { checked -> onCheckedChange(index, checked) },
                cardColor = item.color
            )
        }
    }
}

data class GratitudeItem(
    val text: String,
    val isChecked: Boolean,
    val color: GratitudeCardColor
)

@Preview(showBackground = true)
@Composable
fun OraGratitudeCardPreview() {
    OraTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var text1 by remember { mutableStateOf("Ma famille qui me soutient") }
            var checked1 by remember { mutableStateOf(true) }

            var text2 by remember { mutableStateOf("") }
            var checked2 by remember { mutableStateOf(false) }

            var text3 by remember { mutableStateOf("Le soleil ce matin") }
            var checked3 by remember { mutableStateOf(false) }

            OraGratitudeCard(
                text = text1,
                onTextChange = { text1 = it },
                isChecked = checked1,
                onCheckedChange = { checked1 = it },
                cardColor = GratitudeCardColor.PINK
            )

            OraGratitudeCard(
                text = text2,
                onTextChange = { text2 = it },
                isChecked = checked2,
                onCheckedChange = { checked2 = it },
                cardColor = GratitudeCardColor.ORANGE
            )

            OraGratitudeCard(
                text = text3,
                onTextChange = { text3 = it },
                isChecked = checked3,
                onCheckedChange = { checked3 = it },
                cardColor = GratitudeCardColor.GREEN
            )
        }
    }
}