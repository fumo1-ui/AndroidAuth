package com.androidauth.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidauth.app.data.model.AccountSession
import com.androidauth.app.data.storage.AccountStorage
import com.androidauth.app.data.totp.SteamTotp
import com.androidauth.app.ui.components.AccountCard
import com.androidauth.app.ui.components.AccountDetailSheet
import com.androidauth.app.ui.components.AddAccountSheet
import com.androidauth.app.ui.components.BackupSheet
import com.androidauth.app.ui.components.SettingsSheet
import com.androidauth.app.ui.theme.TotpCriticalColor
import com.androidauth.app.ui.theme.TotpNormalColor
import com.androidauth.app.ui.theme.TotpWarningColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    storage: AccountStorage,
    onPickFiles: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onExportSingleMaFile: (AccountSession) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val accounts by storage.accounts.collectAsState()
    val themeMode by storage.themeMode.collectAsState()
    val accentColor by storage.accentColor.collectAsState()
    val appLanguage by storage.appLanguage.collectAsState()
    val autoCopy by storage.autoCopy.collectAsState()

    val strings = remember(appLanguage) { Strings(appLanguage) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "FAV"
    var remainingSeconds by remember { mutableIntStateOf(SteamTotp.getRemainingSeconds()) }
    var generatedCodes by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Bottom Sheet States
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showBackupSheet by remember { mutableStateOf(false) }
    var selectedAccountForDetail by remember { mutableStateOf<AccountSession?>(null) }

    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val backupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Periodic 30s Steam Guard TOTP Generator Loop
    LaunchedEffect(accounts) {
        while (isActive) {
            val sec = SteamTotp.getRemainingSeconds()
            remainingSeconds = sec

            val newCodes = mutableMapOf<String, String>()
            accounts.forEach { acc ->
                newCodes[acc.id] = SteamTotp.generateAuthCode(acc.sharedSecret)
            }
            generatedCodes = newCodes

            delay(1000L)
        }
    }

    fun showToast(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun copyToClipboard(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        showToast(strings.copiedToast(label, text))
    }

    // Filter accounts by search query and category
    val filteredAccounts = remember(accounts, searchQuery, selectedFilter) {
        accounts.filter { acc ->
            if (selectedFilter == "FAV" && !acc.isFavorite) return@filter false

            if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                acc.accountName.lowercase().contains(q) ||
                        acc.steamLogin.lowercase().contains(q) ||
                        acc.email.lowercase().contains(q) ||
                        acc.steamId.lowercase().contains(q) ||
                        acc.notes.lowercase().contains(q)
            }
        }
    }

    // Smooth animated progress and color
    val animatedProgress by animateFloatAsState(
        targetValue = remainingSeconds / 30f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "totp_progress"
    )

    val targetIndicatorColor = when {
        remainingSeconds <= 5 -> TotpCriticalColor
        remainingSeconds <= 10 -> TotpWarningColor
        else -> MaterialTheme.colorScheme.primary
    }

    val animatedColor by animateColorAsState(
        targetValue = targetIndicatorColor,
        animationSpec = tween(durationMillis = 300),
        label = "totp_color"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.sessionsCount(accounts.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            showToast(strings.syncTimeStart)
                            val offset = SteamTotp.syncSteamTime()
                            showToast(strings.syncTimeDone(offset))
                        }
                    }) {
                        Icon(Icons.Filled.Sync, contentDescription = strings.syncSteamServerTimeBtn)
                    }

                    IconButton(onClick = { showBackupSheet = true }) {
                        Icon(Icons.Filled.Backup, contentDescription = strings.backupTitle)
                    }

                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = strings.settingsTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(strings.addMaFileBtn, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(20.dp)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Material 3 Expressive Countdown Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.countdownLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${remainingSeconds}s",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = animatedColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = animatedColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }

            // Material 3 Expressive Compact Search Bar
            if (accounts.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = strings.searchPlaceholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = strings.closeBtn,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Category Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text(strings.allAccounts, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "FAV",
                            onClick = { selectedFilter = "FAV" },
                            label = { Text(strings.favorites, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            // Accounts List or Empty State
            if (filteredAccounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (accounts.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(92.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.FolderOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(46.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = strings.noSessionsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.noSessionsDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(22.dp))
                            Button(
                                onClick = { showAddSheet = true },
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.addMaFileBtn, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Text(
                            text = "${strings.notFound} \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredAccounts, key = { it.id }) { acc ->
                        val code = generatedCodes[acc.id] ?: "-----"
                        AccountCard(
                            account = acc,
                            currentTotpCode = code,
                            strings = strings,
                            onCopyText = { text, label -> copyToClipboard(text, label) },
                            onToggleFavorite = { id -> storage.toggleFavorite(id) },
                            onOpenDetails = { selectedAccountForDetail = it }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (showAddSheet) {
        AddAccountSheet(
            strings = strings,
            sheetState = addSheetState,
            onDismiss = { showAddSheet = false },
            onPickFiles = {
                showAddSheet = false
                onPickFiles()
            },
            onAccountAdded = { newAcc ->
                storage.saveAccount(newAcc)
                showToast(strings.sessionSaved(newAcc.accountName))
            },
            onCreateDemo = {
                val demo = storage.createDemoAccount()
                showToast(strings.demoCreated(demo.accountName))
            },
            onError = { showToast(it) }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            currentTheme = themeMode,
            currentAccent = accentColor,
            currentLanguage = appLanguage,
            autoCopy = autoCopy,
            strings = strings,
            sheetState = settingsSheetState,
            onDismiss = { showSettingsSheet = false },
            onThemeSelected = { storage.setThemeMode(it) },
            onAccentSelected = { storage.setAccentColor(it) },
            onLanguageSelected = { storage.setAppLanguage(it) },
            onAutoCopyChanged = { storage.setAutoCopy(it) },
            onSyncTime = {
                coroutineScope.launch {
                    val offset = SteamTotp.syncSteamTime()
                    showToast(strings.syncTimeDone(offset))
                }
            }
        )
    }

    if (showBackupSheet) {
        BackupSheet(
            accountsCount = accounts.size,
            strings = strings,
            sheetState = backupSheetState,
            onDismiss = { showBackupSheet = false },
            onExportBackup = {
                showBackupSheet = false
                onExportBackup()
            },
            onImportBackup = {
                showBackupSheet = false
                onImportBackup()
            },
            onClearAll = {
                storage.clearAll()
                showToast(strings.allSessionsCleared)
            }
        )
    }

    selectedAccountForDetail?.let { acc ->
        AccountDetailSheet(
            account = acc,
            strings = strings,
            sheetState = detailSheetState,
            onDismiss = { selectedAccountForDetail = null },
            onSave = { updated ->
                storage.saveAccount(updated)
                showToast(strings.changesSaved)
            },
            onDelete = { id ->
                storage.deleteAccount(id)
                showToast(strings.accountDeleted)
            },
            onExportMaFile = { onExportSingleMaFile(it) },
            onCopyText = { text, label -> copyToClipboard(text, label) }
        )
    }
}
