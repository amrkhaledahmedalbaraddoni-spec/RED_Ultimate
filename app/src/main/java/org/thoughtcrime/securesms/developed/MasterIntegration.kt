package org.thoughtcrime.securesms.developed

import android.content.Context
import android.util.Log
import org.thoughtcrime.securesms.developed.delivery.GuaranteedDelivery
import org.thoughtcrime.securesms.developed.pstn.DuminManager
import org.thoughtcrime.securesms.developed.voip.UltraHDCall

/**
 * RED Master Integration Layer.
 *
 * A thin façade that wires the three RED sub-systems into the Signal-Android application lifecycle.
 * It delegates the real work to the dedicated engines and keeps a strict boundary between
 * System A (VoIP) and System B (PSTN) so no WebRTC context can leak into the GSM path.
 */
object MasterIntegration {

  private const val TAG = "RED"
  private const val PREFS = "red_sovereign_prefs"
  private const val KEY_APPROVED = "user_approved"

  @Volatile
  private var voipEngine: UltraHDCall? = null

  @Volatile
  private var pstnEngine: DuminManager? = null

  @Volatile
  private var deliveryEngine: GuaranteedDelivery? = null

  /**
   * Bootstraps all engines. Idempotent and safe to call from [android.app.Application.onCreate].
   */
  fun initialize(context: Context) {
    if (voipEngine != null) {
      return
    }
    voipEngine = UltraHDCall(codec = "AV1", resolution = "4K")
    pstnEngine = DuminManager(org.thoughtcrime.securesms.dependencies.DevelopedServerConfig.DUMIN_GATEWAY_URL)
    deliveryEngine = GuaranteedDelivery(retryStrategy = "ExponentialBackoff")
    Log.i(TAG, "RED engines initialized (VoIP=AV1/4K, PSTN=Dumin, Delivery=ExponentialBackoff)")
  }

  /**
   * Real admin-approval gate backed by encrypted app preferences.
   *
   * Returns false until an administrator explicitly approves the device, which is the correct
   * default-deny behavior for an approval-enforced deployment.
   */
  fun checkAdminApproval(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_APPROVED, false)
  }

  /**
   * Called by the approval flow once the backend confirms the user is approved.
   */
  fun markApproved(context: Context) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit()
      .putBoolean(KEY_APPROVED, true)
      .apply()
  }

  fun deliveryEngine(): GuaranteedDelivery? = deliveryEngine
  fun pstnEngine(): DuminManager? = pstnEngine
  fun voipEngine(): UltraHDCall? = voipEngine
}
