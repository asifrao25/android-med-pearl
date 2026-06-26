package com.knowledgepearls.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun AccountGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
) {
    val darkTheme = isPearlDarkTheme()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PearlColors.heroPrimary(darkTheme),
            unfocusedTextColor = PearlColors.heroPrimary(darkTheme),
            focusedLabelColor = PearlColors.heroSecondary(darkTheme),
            unfocusedLabelColor = PearlColors.heroSecondary(darkTheme),
            focusedBorderColor = PearlColors.strongBorder(darkTheme),
            unfocusedBorderColor = PearlColors.cardBorder(darkTheme),
            cursorColor = PearlColors.heroPrimary(darkTheme),
            focusedContainerColor = PearlColors.controlFill(darkTheme),
            unfocusedContainerColor = PearlColors.controlFill(darkTheme),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPickerField(
    label: String,
    value: String,
    options: List<String>,
    placeholder: String,
    onSelected: (String) -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(bottom = 8.dp),
            color = PearlColors.heroSecondary(darkTheme),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = value.ifBlank { placeholder },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PearlColors.heroPrimary(darkTheme),
                    unfocusedTextColor = if (value.isBlank()) {
                        PearlColors.heroSecondary(darkTheme)
                    } else {
                        PearlColors.heroPrimary(darkTheme)
                    },
                    focusedBorderColor = PearlColors.strongBorder(darkTheme),
                    unfocusedBorderColor = PearlColors.cardBorder(darkTheme),
                    focusedContainerColor = PearlColors.controlFill(darkTheme),
                    unfocusedContainerColor = PearlColors.controlFill(darkTheme),
                ),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun AccountSectionLabel(text: String, required: Boolean = false) {
    val darkTheme = isPearlDarkTheme()
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (required) "$text *" else text,
            color = PearlColors.heroSecondary(darkTheme),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun AccountToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PearlColors.controlFill(darkTheme), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PearlColors.heroPrimary(darkTheme))
            Text(subtitle, color = PearlColors.heroSecondary(darkTheme), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
