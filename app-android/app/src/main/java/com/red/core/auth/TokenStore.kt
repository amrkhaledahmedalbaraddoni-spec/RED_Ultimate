package com.red.core.auth

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
  @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
  private val prefs: SharedPreferences by lazy {
    context.getSharedPreferences("red_auth", Context.MODE_PRIVATE)
  }

  fun saveToken(token: String, userId: String) {
    prefs.edit().putString("token", token).putString("userId", userId).apply()
  }

  fun getToken(): String? = prefs.getString("token", null)

  fun getUserId(): String? = prefs.getString("userId", null)

  fun clear() {
    prefs.edit().clear().apply()
  }
}
