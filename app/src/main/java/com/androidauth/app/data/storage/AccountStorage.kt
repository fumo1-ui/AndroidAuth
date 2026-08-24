package com.androidauth.app.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.androidauth.app.data.model.AccountSession
import com.androidauth.app.data.totp.SteamTotp
import com.androidauth.app.ui.AppLanguage
import com.androidauth.app.ui.theme.AccentColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class AccountStorage(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("AndroidAuth_Prefs", Context.MODE_PRIVATE)

    private val _accounts = MutableStateFlow<List<AccountSession>>(emptyList())
    val accounts: StateFlow<List<AccountSession>> = _accounts.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentColor = MutableStateFlow(loadAccentColor())
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()

    private val _appLanguage = MutableStateFlow(loadAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _autoCopy = MutableStateFlow(prefs.getBoolean("auto_copy", true))
    val autoCopy: StateFlow<Boolean> = _autoCopy.asStateFlow()

    init {
        loadAccountsFromDisk()
    }

    private fun loadAccountsFromDisk() {
        val raw = prefs.getString("accounts_json", "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<AccountSession>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(AccountSession.fromJson(obj))
            }
            sortAndEmit(list)
        } catch (e: Exception) {
            e.printStackTrace()
            _accounts.value = emptyList()
        }
    }

    private fun persistAccounts(list: List<AccountSession>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it.toJson()) }
        prefs.edit().putString("accounts_json", jsonArray.toString()).apply()
        sortAndEmit(list)
    }

    private fun sortAndEmit(list: List<AccountSession>) {
        val sorted = list.sortedWith(
            compareByDescending<AccountSession> { it.isFavorite }
                .thenBy { it.accountName.lowercase(Locale.ROOT) }
        )
        _accounts.value = sorted
    }

    fun saveAccount(account: AccountSession) {
        val current = _accounts.value.toMutableList()
        val index = current.indexOfFirst { it.id == account.id }
        val updated = account.copy(updatedAt = System.currentTimeMillis())
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(0, updated)
        }
        persistAccounts(current)
    }

    fun saveMultipleAccounts(newAccounts: List<AccountSession>): Int {
        if (newAccounts.isEmpty()) return 0
        val current = _accounts.value.toMutableList()
        newAccounts.forEach { acc ->
            val index = current.indexOfFirst { it.id == acc.id }
            if (index >= 0) {
                current[index] = acc
            } else {
                current.add(0, acc)
            }
        }
        persistAccounts(current)
        return newAccounts.size
    }

    fun toggleFavorite(id: String) {
        val current = _accounts.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val acc = current[index]
            current[index] = acc.copy(isFavorite = !acc.isFavorite, updatedAt = System.currentTimeMillis())
            persistAccounts(current)
        }
    }

    fun deleteAccount(id: String) {
        val current = _accounts.value.filter { it.id != id }
        persistAccounts(current)
    }

    fun clearAll() {
        persistAccounts(emptyList())
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setAccentColor(accent: AccentColor) {
        prefs.edit().putString("accent_color", accent.name).apply()
        _accentColor.value = accent
    }

    private fun loadAccentColor(): AccentColor {
        val name = prefs.getString("accent_color", AccentColor.MATERIAL_YOU.name) ?: AccentColor.MATERIAL_YOU.name
        return try {
            AccentColor.valueOf(name)
        } catch (e: Exception) {
            AccentColor.MATERIAL_YOU
        }
    }

    fun setAppLanguage(lang: AppLanguage) {
        prefs.edit().putString("app_language", lang.name).apply()
        _appLanguage.value = lang
    }

    private fun loadAppLanguage(): AppLanguage {
        val name = prefs.getString("app_language", AppLanguage.RU.name) ?: AppLanguage.RU.name
        return try {
            AppLanguage.valueOf(name)
        } catch (e: Exception) {
            AppLanguage.RU
        }
    }

    fun setAutoCopy(enabled: Boolean) {
        prefs.edit().putBoolean("auto_copy", enabled).apply()
        _autoCopy.value = enabled
    }

    fun createDemoAccount(): AccountSession {
        val demoNames = listOf("cs2_trader", "dota2_smurf", "steam_vault", "knife_collector", "cyber_ninja", "phoenix_king")
        val chosenName = demoNames.random() + "_" + (10..99).random()

        val randomBytes = ByteArray(20)
        SecureRandom().nextBytes(randomBytes)
        val fakeSharedSecret = Base64.encodeToString(randomBytes, Base64.NO_WRAP)

        val demoAccount = AccountSession(
            id = "acc_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6),
            accountName = chosenName,
            sharedSecret = fakeSharedSecret,
            identitySecret = fakeSharedSecret,
            revocationCode = "R" + (10000..99999).random(),
            steamId = "76561198" + (100000000..999999999).random(),
            serialNumber = "189234891234",
            steamLogin = chosenName,
            steamPassword = "SteamPassword@" + (100..999).random(),
            email = "$chosenName@gmail.com",
            emailPassword = "MailPassword#" + (100..999).random(),
            notes = "Тестовая сессия с живым расчетом Steam 2FA TOTP",
            isFavorite = false,
            rawMaFile = JSONObject().apply {
                put("shared_secret", fakeSharedSecret)
                put("account_name", chosenName)
                put("steamid", "76561198000000000")
            }.toString(2)
        )

        saveAccount(demoAccount)
        return demoAccount
    }

    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("app", "AndroidAuth")
        root.put("version", "1.0")
        root.put("exported_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("count", _accounts.value.size)

        val accountsArray = JSONArray()
        _accounts.value.forEach { accountsArray.put(it.toJson()) }
        root.put("accounts", accountsArray)

        return root.toString(2)
    }

    fun importBackupJson(content: String): Int {
        val clean = content.replace("\uFEFF", "").trim()
        val imported = mutableListOf<AccountSession>()

        if (clean.startsWith("[")) {
            val arr = JSONArray(clean)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                imported.add(AccountSession.fromJson(obj))
            }
        } else {
            val root = JSONObject(clean)
            if (root.has("accounts")) {
                val arr = root.getJSONArray("accounts")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    imported.add(AccountSession.fromJson(obj))
                }
            } else if (root.has("shared_secret") || root.has("sharedSecret")) {
                imported.add(SteamTotp.parseMaFile(clean))
            }
        }

        if (imported.isNotEmpty()) {
            saveMultipleAccounts(imported)
        }
        return imported.size
    }
}
