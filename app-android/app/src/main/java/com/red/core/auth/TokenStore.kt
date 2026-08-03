package com.red.core.auth

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure token store — holds the JWT auth token and user ID.
 * Uses SharedPreferences for simple storage. In production,
 * this should use EncryptedSharedPreferences for better security.
 */
@Singleton
class TokenStore @Inject constructor(
  @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
  private val prefs: SharedPreferences by lazy {
    context.getSharedPreferences("red_auth", Context.MODE_PRIVATE)
  }

  fun saveToken(token: String, userId: String) {
    prefs.edit()
      .putString("token", token)
      .putString("userId", userId)
      .putLong("tokenSavedAt", System.currentTimeMillis())
      .apply()
  }

  fun getToken(): String? = prefs.getString("token", null)

  fun getUserId(): String? = prefs.getString("userId", null)

  fun getTokenAge(): Long {
    val savedAt = prefs.getLong("tokenSavedAt", 0)
    return if (savedAt > 0) System.currentTimeMillis() - savedAt else 0
  }

  fun isTokenExpired(maxAgeMs: Long = 24 * 60 * 60 * 1000L): Boolean {
    return getTokenAge() > maxAgeMs
  }

  fun clear() {
    prefs.edit().clear().apply()
  }
}
