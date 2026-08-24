package com.androidauth.app.data.model

import org.json.JSONObject
import java.util.UUID

data class AccountSession(
    val id: String = UUID.randomUUID().toString(),
    val accountName: String,
    val sharedSecret: String,
    val identitySecret: String = "",
    val revocationCode: String = "",
    val steamId: String = "",
    val serialNumber: String = "",
    val steamLogin: String = accountName,
    val steamPassword: String = "",
    val email: String = "",
    val emailPassword: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val rawMaFile: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("account_name", accountName)
        obj.put("shared_secret", sharedSecret)
        obj.put("identity_secret", identitySecret)
        obj.put("revocation_code", revocationCode)
        obj.put("steamid", steamId)
        obj.put("serial_number", serialNumber)
        obj.put("steam_login", steamLogin)
        obj.put("steam_password", steamPassword)
        obj.put("email", email)
        obj.put("email_password", emailPassword)
        obj.put("notes", notes)
        obj.put("favorite", isFavorite)
        obj.put("raw_mafile", rawMaFile)
        obj.put("created_at", createdAt)
        obj.put("updated_at", updatedAt)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): AccountSession {
            return AccountSession(
                id = obj.optString("id", UUID.randomUUID().toString()),
                accountName = obj.optString("account_name", "Account"),
                sharedSecret = obj.optString("shared_secret", ""),
                identitySecret = obj.optString("identity_secret", ""),
                revocationCode = obj.optString("revocation_code", ""),
                steamId = obj.optString("steamid", ""),
                serialNumber = obj.optString("serial_number", ""),
                steamLogin = obj.optString("steam_login", obj.optString("account_name", "")),
                steamPassword = obj.optString("steam_password", ""),
                email = obj.optString("email", ""),
                emailPassword = obj.optString("email_password", ""),
                notes = obj.optString("notes", ""),
                isFavorite = obj.optBoolean("favorite", false),
                rawMaFile = obj.optString("raw_mafile", ""),
                createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                updatedAt = obj.optLong("updated_at", System.currentTimeMillis())
            )
        }
    }
}
