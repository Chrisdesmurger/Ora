package com.ora.wellbeing.presentation.screens.profile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Dropdown selector component for profile fields (e.g., Gender)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDropdown(
    value: String?,
    onValueChange: (String?) -> Unit,
    label: String,
    options: List<DropdownOption>,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.value == value }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption?.label ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onValueChange(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Data class for dropdown options
 */
data class DropdownOption(
    val value: String?,
    val label: String
)
