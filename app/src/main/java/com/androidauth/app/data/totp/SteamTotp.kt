package com.androidauth.app.data.totp

import android.util.Base64
import com.androidauth.app.data.model.AccountSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.util.UUID
import java.util.regex.Pattern
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SteamTotp {

    private const val STEAM_CHARS = "23456789BCDFGHJKMNPQRTVWXY"
    var timeOffsetSeconds: Long = 0

    fun getSteamTimeSeconds(): Long {
        return (System.currentTimeMillis() / 1000L) + timeOffsetSeconds
    }

    fun getRemainingSeconds(): Int {
        val sec = getSteamTimeSeconds()
        return (30 - (sec % 30)).toInt()
    }

    fun getRemainingProgress(): Float {
        return getRemainingSeconds() / 30f
    }

    fun generateAuthCode(sharedSecret: String, customTimeSec: Long? = null): String {
        if (sharedSecret.isBlank()) return "ERR--"

        try {
            val time = customTimeSec ?: getSteamTimeSeconds()
            val timeStep = time / 30L

            val buffer = ByteBuffer.allocate(8)
            buffer.putInt(0) // Higher 32 bits 0
            buffer.putInt(timeStep.toInt())
            val timeBytes = buffer.array()

            // Decode base64 secret
            var cleanSecret = sharedSecret.trim().replace('-', '+').replace('_', '/')
            while (cleanSecret.length % 4 != 0) {
                cleanSecret += "="
            }
            val secretBytes = Base64.decode(cleanSecret, Base64.DEFAULT)

            val mac = Mac.getInstance("HmacSHA1")
            val keySpec = SecretKeySpec(secretBytes, "HmacSHA1")
            mac.init(keySpec)
            val hash = mac.doFinal(timeBytes)

            val offset = hash[19].toInt() and 0x0F
            var fullcode = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)

            val codeBuilder = StringBuilder()
            for (i in 0 until 5) {
                codeBuilder.append(STEAM_CHARS[fullcode % 26])
                fullcode /= 26
            }

            return codeBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return "ERROR"
        }
    }

    fun parseMaFile(content: String, fallbackFileName: String? = null): AccountSession {
        val cleanText = content.replace("\uFEFF", "").trim()
        if (cleanText.isBlank()) {
            throw IllegalArgumentException("Файл пуст")
        }

        var jsonObject: JSONObject? = null
        try {
            jsonObject = JSONObject(cleanText)
        } catch (jsonErr: Exception) {
            // Regex recovery if JSON is slightly malformed
            val secretMatcher = Pattern.compile("[\"']?(?:shared_secret|sharedSecret)[\"']?\\s*[:=]\\s*[\"']([^\"'\\r\\n]+)[\"']", Pattern.CASE_INSENSITIVE).matcher(cleanText)
            val nameMatcher = Pattern.compile("[\"']?(?:account_name|accountName)[\"']?\\s*[:=]\\s*[\"']([^\"'\\r\\n]+)[\"']", Pattern.CASE_INSENSITIVE).matcher(cleanText)
            val idMatcher = Pattern.compile("[\"']?(?:steamid|SteamID)[\"']?\\s*[:=]\\s*[\"']?(\\d{10,20})[\"']?", Pattern.CASE_INSENSITIVE).matcher(cleanText)
            val rcodeMatcher = Pattern.compile("[\"']?(?:revocation_code|revocationCode|secret_1)[\"']?\\s*[:=]\\s*[\"']([^\"'\\r\\n]+)[\"']", Pattern.CASE_INSENSITIVE).matcher(cleanText)

            if (secretMatcher.find()) {
                val secret = secretMatcher.group(1)?.trim() ?: ""
                val name = if (nameMatcher.find()) nameMatcher.group(1)?.trim() else null
                val sid = if (idMatcher.find()) idMatcher.group(1)?.trim() else ""
                val rcode = if (rcodeMatcher.find()) rcodeMatcher.group(1)?.trim() else ""

                val derivedName = name ?: fallbackFileName?.replace(Regex("(?i)\\.mafile$"), "") ?: "Steam_Account"

                return AccountSession(
                    id = "acc_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6),
                    accountName = derivedName,
                    sharedSecret = secret,
                    steamId = sid ?: "",
                    revocationCode = rcode ?: "",
                    rawMaFile = cleanText
                )
            }
        }

        if (jsonObject == null) {
            throw IllegalArgumentException("Не удалось распарсить .maFile")
        }

        fun getVal(obj: JSONObject, vararg keys: String): String {
            for (k in keys) {
                if (obj.has(k) && !obj.isNull(k)) {
                    val v = obj.optString(k, "").trim()
                    if (v.isNotBlank()) return v
                }
                // Case-insensitive check
                val iter = obj.keys()
                while (iter.hasNext()) {
                    val actualKey = iter.next()
                    if (actualKey.equals(k, ignoreCase = true)) {
                        val v = obj.optString(actualKey, "").trim()
                        if (v.isNotBlank()) return v
                    }
                }
            }
            return ""
        }

        val sessionObj = jsonObject.optJSONObject("Session") ?: jsonObject.optJSONObject("session") ?: JSONObject()

        val sharedSecret = getVal(jsonObject, "shared_secret", "sharedSecret").ifBlank {
            getVal(sessionObj, "shared_secret", "sharedSecret")
        }

        val identitySecret = getVal(jsonObject, "identity_secret", "identitySecret").ifBlank {
            getVal(sessionObj, "identity_secret", "identitySecret")
        }

        var accountName = getVal(jsonObject, "account_name", "accountName").ifBlank {
            getVal(sessionObj, "AccountName", "account_name", "accountName")
        }

        if (accountName.isBlank()) {
            accountName = fallbackFileName?.replace(Regex("(?i)\\.mafile$"), "") ?: ("Steam_" + (1000..9999).random())
        }

        val steamId = getVal(jsonObject, "steamid", "SteamID", "steam_id").ifBlank {
            getVal(sessionObj, "SteamID", "steamid", "steam_id")
        }

        val revocationCode = getVal(jsonObject, "revocation_code", "revocationCode", "secret_1", "secret1")

        if (sharedSecret.isBlank() && (jsonObject.has("encryption_iv") || jsonObject.has("salt") || jsonObject.has("encrypted"))) {
            throw IllegalArgumentException("Файл зашифрован паролем SDA. Пожалуйста, снимите пароль в Steam Desktop Authenticator перед экспортом.")
        }

        if (sharedSecret.isBlank()) {
            throw IllegalArgumentException("Не найден ключ shared_secret в файле .maFile")
        }

        return AccountSession(
            id = "acc_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6),
            accountName = accountName,
            sharedSecret = sharedSecret,
            identitySecret = identitySecret,
            revocationCode = revocationCode,
            steamId = steamId,
            serialNumber = getVal(jsonObject, "serial_number", "serialNumber"),
            steamLogin = getVal(jsonObject, "steam_login", "login").ifBlank { accountName },
            steamPassword = getVal(jsonObject, "steam_password", "password"),
            email = getVal(jsonObject, "email", "mail"),
            emailPassword = getVal(jsonObject, "email_password", "mail_password"),
            notes = getVal(jsonObject, "notes", "comment"),
            isFavorite = jsonObject.optBoolean("favorite", false),
            rawMaFile = cleanText
        )
    }

    suspend fun syncSteamTime(): Long = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val url = URL("https://api.steampowered.com/ITwoFactorService/QueryTime/v0001/")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.doOutput = true

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val resp = root.optJSONObject("response")
                if (resp != null && resp.has("server_time")) {
                    val serverTime = resp.getLong("server_time")
                    val latency = ((System.currentTimeMillis() - start) / 2000L)
                    val localTime = System.currentTimeMillis() / 1000L
                    timeOffsetSeconds = (serverTime + latency) - localTime
                    return@withContext timeOffsetSeconds
                }
            }
        } catch (e: Exception) {
            // Local clock fallback
        }
        return@withContext 0L
    }
}
