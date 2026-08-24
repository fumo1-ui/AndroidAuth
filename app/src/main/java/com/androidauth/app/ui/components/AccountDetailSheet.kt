package com.androidauth.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidauth.app.data.model.AccountSession
import com.androidauth.app.ui.Strings
import com.androidauth.app.ui.components.cards.LabelCard
import com.androidauth.app.ui.components.cards.TextFieldCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailSheet(
    account: AccountSession,
    strings: Strings,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (AccountSession) -> Unit,
    onDelete: (String) -> Unit,
    onExportMaFile: (AccountSession) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    var sessionName by remember(account) { mutableStateOf(account.accountName) }

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
                Column {
                    Text(
                        text = strings.detailsSheetTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (account.steamId.isNotBlank()) "SteamID: ${account.steamId}" else strings.activeSession,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = strings.closeBtn)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Session Name (MD3 TextFieldCard)
            TextFieldCard(
                label = strings.sessionNameLabel,
                value = sessionName,
                onValueChange = { sessionName = it },
                placeholder = strings.sessionNamePlaceholder,
                icon = Icons.Filled.Edit,
                cardShapeTop = 20,
                cardShapeBottom = 20,
                cardSpacer = 0f
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Steam Login (MD3 LabelCard with Copy action)
            LabelCard(
                title = account.steamLogin.ifBlank { account.accountName },
                subtitle = "Steam Login",
                label = strings.steamLoginFullLabel,
                icon = Icons.Filled.AccountCircle,
                cardShapeTop = 20,
                cardShapeBottom = 20,
                cardSpacer = 0f,
                trailingContent = {
                    IconButton(
                        onClick = { onCopyText(account.steamLogin.ifBlank { account.accountName }, "Логин Steam") }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = strings.copyBtn,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. R-Code (MD3 LabelCard with Copy action)
            LabelCard(
                title = if (account.revocationCode.isNotBlank()) account.revocationCode else strings.notSpecified,
                subtitle = "Revocation Code",
                label = strings.rcodeLabel,
                icon = Icons.Filled.Key,
                cardShapeTop = 20,
                cardShapeBottom = 20,
                cardSpacer = 0f,
                trailingContent = if (account.revocationCode.isNotBlank()) {
                    {
                        IconButton(
                            onClick = { onCopyText(account.revocationCode, "R-Code") }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = strings.copyBtn,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else null
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Actions: 1. Save Session Name Button
            Button(
                onClick = {
                    val updated = account.copy(
                        accountName = sessionName.ifBlank { account.accountName }
                    )
                    onSave(updated)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.saveNameBtn, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions: 2. Export single .maFile
            FilledTonalButton(
                onClick = { onExportMaFile(account) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.exportMaFileBtn, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions: 3. Delete Session Button
            OutlinedButton(
                onClick = {
                    onDelete(account.id)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.deleteAccountBtn, fontWeight = FontWeight.Medium)
            }
        }
    }
}
