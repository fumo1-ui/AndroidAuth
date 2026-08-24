package com.androidauth.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.androidauth.app.data.model.AccountSession
import com.androidauth.app.data.storage.AccountStorage
import com.androidauth.app.data.totp.SteamTotp
import com.androidauth.app.ui.MainScreen
import com.androidauth.app.ui.theme.AndroidAuthTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    private lateinit var storage: AccountStorage
    private lateinit var filePickerLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var backupImportLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var backupExportLauncher: ActivityResultLauncher<String>
    private lateinit var singleMaFileExportLauncher: ActivityResultLauncher<String>

    private var pendingExportContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        storage = AccountStorage(applicationContext)

        // 1. SAF Multiple File Picker (Files by Google / System Documents)
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
            if (!uris.isNullOrEmpty()) {
                readAndImportUris(uris)
            }
        }

        // 2. Backup Import Launcher
        backupImportLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let { importBackupFromUri(it) }
        }

        // 3. Backup Export Launcher
        backupExportLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let { writeTextToUri(it, pendingExportContent) }
        }

        // 4. Single .maFile Export Launcher
        singleMaFileExportLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let { writeTextToUri(it, pendingExportContent) }
        }

        // 5. Handle initial launch intent
        handleIntent(intent)

        setContent {
            val themeMode by storage.themeMode.collectAsState()
            val accentColor by storage.accentColor.collectAsState()

            AndroidAuthTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                MainScreen(
                    storage = storage,
                    onPickFiles = {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                    onExportBackup = {
                        pendingExportContent = storage.exportBackupJson()
                        val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                        backupExportLauncher.launch("AndroidAuth_Backup_$dateStr.json")
                    },
                    onImportBackup = {
                        backupImportLauncher.launch(arrayOf("*/*", "application/json"))
                    },
                    onExportSingleMaFile = { acc ->
                        pendingExportContent = acc.rawMaFile.ifBlank { acc.toJson().toString(2) }
                        singleMaFileExportLauncher.launch("${acc.accountName}.maFile")
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val uriList = mutableListOf<Uri>()

        if (Intent.ACTION_VIEW == action) {
            intent.data?.let { uriList.add(it) }
            if (intent.clipData != null) {
                for (i in 0 until intent.clipData!!.itemCount) {
                    uriList.add(intent.clipData!!.getItemAt(i).uri)
                }
            }
        } else if (Intent.ACTION_SEND == action) {
            if (intent.clipData != null && intent.clipData!!.itemCount > 0) {
                for (i in 0 until intent.clipData!!.itemCount) {
                    uriList.add(intent.clipData!!.getItemAt(i).uri)
                }
            } else {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (uri != null) uriList.add(uri)
                else intent.data?.let { uriList.add(it) }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE == action) {
            if (intent.clipData != null) {
                for (i in 0 until intent.clipData!!.itemCount) {
                    uriList.add(intent.clipData!!.getItemAt(i).uri)
                }
            } else {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (uris != null) uriList.addAll(uris)
            }
        }

        if (uriList.isNotEmpty()) {
            readAndImportUris(uriList)
        }
    }

    private fun readAndImportUris(uris: List<Uri>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val importedAccounts = mutableListOf<AccountSession>()
            var failedCount = 0

            for (uri in uris) {
                try {
                    val fileName = getFileNameFromUri(uri)
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val content = reader.readText()
                        val parsed = SteamTotp.parseMaFile(content, fileName)
                        importedAccounts.add(parsed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    failedCount++
                }
            }

            withContext(Dispatchers.Main) {
                if (importedAccounts.isNotEmpty()) {
                    storage.saveMultipleAccounts(importedAccounts)
                    Toast.makeText(
                        this@MainActivity,
                        "Успешно импортировано сессий: ${importedAccounts.size}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                if (failedCount > 0 && importedAccounts.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Не удалось распознать файлы ($failedCount)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun importBackupFromUri(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = BufferedReader(InputStreamReader(inputStream)).readText()
                    val count = storage.importBackupJson(content)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Восстановлено сессий: $count", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка импорта бэкапа: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun writeTextToUri(uri: Uri, text: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(text.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Файл успешно сохранен", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка сохранения файла: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name: String? = null
        if ("content" == uri.scheme) {
            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = cursor.getString(index)
                        }
                    }
                }
            } catch (ignored: Exception) {}
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/') ?: -1
            if (cut != -1 && name != null) {
                name = name!!.substring(cut + 1)
            }
        }
        return name ?: "session.maFile"
    }
}
