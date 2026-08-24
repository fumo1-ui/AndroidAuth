package com.androidauth.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidauth.app.data.storage.ThemeMode
import com.androidauth.app.ui.AppLanguage
import com.androidauth.app.ui.Strings
import com.androidauth.app.ui.components.cards.CardSwitch
import com.androidauth.app.ui.components.cards.SettingsItemCard
import com.androidauth.app.ui.theme.AccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    currentTheme: ThemeMode,
    currentAccent: AccentColor,
    currentLanguage: AppLanguage,
    autoCopy: Boolean,
    strings: Strings,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onAutoCopyChanged: (Boolean) -> Unit,
    onSyncTime: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = strings.closeBtn)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Accent Theme Card (MD3 Card Design)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = strings.accentThemeTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = currentAccent.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AccentColor.values().forEach { accent ->
                            val isSelected = currentAccent == accent
                            val isMaterialYou = accent == AccentColor.MATERIAL_YOU

                            val dynamicBrush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4285F4),
                                    Color(0xFFEA4335),
                                    Color(0xFFFBBC05),
                                    Color(0xFF34A853)
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (isMaterialYou) {
                                            Modifier.background(dynamicBrush)
                                        } else {
                                            Modifier.background(accent.previewColor)
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onAccentSelected(accent) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (isMaterialYou) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = "Material You",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Display Theme Mode Card (MD3 Card Design)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = strings.displayModeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentTheme == ThemeMode.SYSTEM,
                            onClick = { onThemeSelected(ThemeMode.SYSTEM) },
                            label = { Text(strings.themeSystem) },
                            leadingIcon = { Icon(Icons.Filled.BrightnessAuto, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentTheme == ThemeMode.LIGHT,
                            onClick = { onThemeSelected(ThemeMode.LIGHT) },
                            label = { Text(strings.themeLight) },
                            leadingIcon = { Icon(Icons.Filled.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentTheme == ThemeMode.DARK,
                            onClick = { onThemeSelected(ThemeMode.DARK) },
                            label = { Text(strings.themeDark) },
                            leadingIcon = { Icon(Icons.Filled.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Language Selector Card (MD3 Card Design)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = strings.languageTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentLanguage == AppLanguage.RU,
                            onClick = { onLanguageSelected(AppLanguage.RU) },
                            label = { Text("Русский (RU)") },
                            leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentLanguage == AppLanguage.EN,
                            onClick = { onLanguageSelected(AppLanguage.EN) },
                            label = { Text("English (EN)") },
                            leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Auto-Copy Switch Card (MD3 CardSwitch Component)
            CardSwitch(
                title = strings.autoCopyTitle,
                subtitle = strings.autoCopyDesc,
                cardShapeTop = 20,
                cardShapeBottom = 20,
                cardSpacer = 0f,
                icon = Icons.Filled.ContentCopy,
                switchState = autoCopy,
                onSwitchChange = onAutoCopyChanged
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Sync Steam Server Time (MD3 SettingsItemCard Component)
            SettingsItemCard(
                title = strings.syncSteamServerTimeBtn,
                subtitle = "Steam Web API Time Sync",
                cardShapeTop = 20,
                cardShapeBottom = 20,
                cardSpacer = 0f,
                icon = Icons.Filled.Sync,
                iconTint = MaterialTheme.colorScheme.primaryContainer,
                iconSubTint = MaterialTheme.colorScheme.onPrimaryContainer,
                onItemClick = onSyncTime
            )
        }
    }
}
