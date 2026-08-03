package org.thoughtcrime.securesms.developed

import android.content.Context
import android.util.Log
import org.thoughtcrime.securesms.developed.delivery.GuaranteedDelivery
import org.thoughtcrime.securesms.developed.pstn.DuminManager
import org.thoughtcrime.securesms.developed.voip.UltraHDCall
import org.thoughtcrime.securesms.dependencies.DevelopedServerConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object MasterIntegration {

  private const val TAG = "RED"
  private const val PREFS = "red_sovereign_prefs"
  private const val KEY_APPROVED = "user_approved"
  private const val KEY_TOKEN = "auth_token"

  @Volatile private var voipEngine: UltraHDCall? = null
  @Volatile private var pstnEngine: DuminManager? = null
  @Volatile private var deliveryEngine: GuaranteedDelivery? = null

  fun initialize(context: Context) {
    if (voipEngine != null) return
    voipEngine = UltraHDCall(codec = "AV1", resolution = "4K")
    pstnEngine = DuminManager(DevelopedServerConfig.DUMIN_GATEWAY_URL)
    deliveryEngine = GuaranteedDelivery(retryStrategy = "ExponentialBackoff")
    Log.i(TAG, "RED engines initialized (VoIP=AV1/4K, PSTN=Dumin, Delivery=ExponentialBackoff)")
  }

  fun checkAdminApproval(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_APPROVED, false)
  }

  fun markApproved(context: Context) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit().putBoolean(KEY_APPROVED, true).apply()
  }

  fun storeToken(context: Context, token: String) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit().putString(KEY_TOKEN, token).apply()
  }

  /**
   * Server-backed approval verification. Makes a real API call to /api/auth/status
   * and updates the local flag accordingly. Runs on a background thread.
   */
  fun verifyApprovalFromServer(context: Context) {
    val token = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .getString(KEY_TOKEN, null) ?: return
    thread(name = "red-approval-verify", isDaemon = true) {
      try {
        val url = URL("${DevelopedServerConfig.SIGNAL_URL}/api/auth/status")
        val conn = (url.openConnection() as HttpURLConnection).apply {
          requestMethod = "GET"
          setRequestProperty("Authorization", "Bearer $token")
          connectTimeout = 5000
          readTimeout = 5000
        }
        val code = conn.responseCode
        if (code == 200) {
          val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
          when {
            body.contains("\"APPROVED\"") -> {
              markApproved(context)
              Log.i(TAG, "Server confirmed APPROVED")
            }
            body.contains("\"BANNED\"") || body.contains("\"REJECTED\"") -> {
              context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_APPROVED, false).apply()
              Log.w(TAG, "Server reported BANNED/REJECTED")
            }
            else -> Log.i(TAG, "Server status: $body")
          }
        } else {
          Log.w(TAG, "Approval check failed: HTTP $code")
        }
        conn.disconnect()
      } catch (e: Exception) {
        Log.w(TAG, "Approval check error: ${e.message}")
      }
    }
  }

  fun deliveryEngine(): GuaranteedDelivery? = deliveryEngine
  fun pstnEngine(): DuminManager? = pstnEngine
  fun voipEngine(): UltraHDCall? = voipEngine
}
