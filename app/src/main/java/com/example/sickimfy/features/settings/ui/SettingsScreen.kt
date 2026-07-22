package com.example.sickimfy.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sickimfy.R
import com.example.sickimfy.core.data.preferences.ThemeMode
import com.example.sickimfy.core.data.preferences.UserPreferences
import com.example.sickimfy.core.designsystem.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: UserPreferences,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onApiBaseUrlChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
        ) {
            // ── Server URL ─────────────────────────
            SettingsSectionCard(
                icon = Icons.Default.Link,
                title = stringResource(id = R.string.auth_server_url_label)
            ) {
                OutlinedTextField(
                    value = state.apiBaseUrl,
                    onValueChange = onApiBaseUrlChange,
                    label = { Text(stringResource(id = R.string.auth_server_url_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary
                    ),
                    singleLine = true
                )
            }

            // ── Theme Mode ───────────────────────
            SettingsSectionCard(
                icon = Icons.Default.ColorLens,
                title = stringResource(id = R.string.theme_settings)
            ) {
                val themeOptions = listOf(
                    ThemeMode.SYSTEM to stringResource(id = R.string.theme_option_system),
                    ThemeMode.LIGHT to stringResource(id = R.string.theme_option_light),
                    ThemeMode.DARK to stringResource(id = R.string.theme_option_dark)
                )
                themeOptions.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeChange(mode) }
                            .padding(vertical = Dimens.paddingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.themeMode == mode,
                            onClick = { onThemeChange(mode) }
                        )
                        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ── Language ─────────────────────────
            SettingsSectionCard(
                icon = Icons.Default.Language,
                title = stringResource(id = R.string.language_settings)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showLanguageDialog = true }
                        .padding(vertical = Dimens.paddingSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.language_selected))
                    Text(
                        if (state.languageCode == "fa") stringResource(R.string.language_option_persian)
                        else stringResource(R.string.language_option_english),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Font Scale ───────────────────────
            SettingsSectionCard(
                icon = Icons.Default.TextFields,
                title = stringResource(id = R.string.settings_font_scale)
            ) {
                Text(
                    text = stringResource(id = R.string.font_scale_current_format, state.fontScale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                Slider(
                    value = state.fontScale,
                    onValueChange = onFontScaleChange,
                    valueRange = 0.85f..1.3f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surface
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.font_scale_small), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(text = stringResource(id = R.string.font_scale_large), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showLanguageDialog) {
        val languageOptions = listOf(
            "fa" to stringResource(R.string.language_option_persian),
            "en" to stringResource(R.string.language_option_english)
        )
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language_settings)) },
            text = {
                Column {
                    languageOptions.forEach { (code, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onLanguageChange(code)
                                showLanguageDialog = false
                            }.padding(vertical = Dimens.paddingSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.languageCode == code,
                                onClick = {
                                    onLanguageChange(code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(Modifier.width(Dimens.paddingSmall))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = Dimens.paddingSmall)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}
