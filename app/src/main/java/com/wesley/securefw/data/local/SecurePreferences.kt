package com.wesley.securefw.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SecurePreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Estado observável para a UI (opcional, mas útil)
    private val _userId = MutableStateFlow<String?>(getUserId())
    val userId: StateFlow<String?> = _userId

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
        _userId.value = userId
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _userId.value = null
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null
    }
}

