package com.androidauth.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidauth.app.ui.Strings
import com.androidauth.app.ui.components.cards.SettingsItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSheet(
    accountsCount: Int,
    strings: Strings,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onClearAll: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.deleteConfirmTitle) },
            text = { Text(strings.deleteConfirmDesc(accountsCount)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearAll()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.deleteAllConfirmBtn, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(strings.cancelBtn)
                }
            }
        )
    }

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
                    text = strings.backupTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = strings.closeBtn)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Export Backup Card (MD3 SettingsItemCard)
            SettingsItemCard(
                title = strings.exportBackupBtn(accountsCount),
                subtitle = "JSON Export",
                cardShapeTop = 20,
                cardShapeBottom = 20,
                cardSpacer = 0f,
                icon = Icons.Filled.FileDownload,
                iconTint = MaterialTheme.colorScheme.primaryContainer,
                iconSubTint = MaterialTheme.colorScheme.onPrimaryContainer,
                onItemClick = onExportBackup
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Restore Backup Card (MD3 SettingsItemCard)
            SettingsItemCard(
                title = strings.restoreBackupBtn,
                subtitle = "JSON Import",
                cardShapeTop = 20,
                cardShapeBottom = 20,
                cardSpacer = 0f,
                icon = Icons.Filled.FileUpload,
                iconTint = MaterialTheme.colorScheme.secondaryContainer,
                iconSubTint = MaterialTheme.colorScheme.onSecondaryContainer,
                onItemClick = onImportBackup
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Danger Zone: Clear All Sessions Card (MD3 SettingsItemCard)
            SettingsItemCard(
                title = strings.clearAllSessionsBtn,
                subtitle = "Delete all saved accounts",
                cardShapeTop = 20,
                cardShapeBottom = 20,
                cardSpacer = 0f,
                icon = Icons.Filled.DeleteSweep,
                iconTint = MaterialTheme.colorScheme.errorContainer,
                iconSubTint = MaterialTheme.colorScheme.onErrorContainer,
                onItemClick = { showClearDialog = true }
            )
        }
    }
}
