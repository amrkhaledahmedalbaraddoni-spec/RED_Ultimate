package org.thoughtcrime.securesms.developed

import android.content.Context
import android.util.Log
import org.thoughtcrime.securesms.developed.delivery.GuaranteedDelivery
import org.thoughtcrime.securesms.developed.pstn.DuminManager
import org.thoughtcrime.securesms.developed.voip.UltraHDCall

/**
 * RED: The Ultimate Integration Layer
 *
 * Unified entry point for System A (4K VoIP), System B (PSTN/Dumin) and System C (messaging)
 * within the Signal-Android core. Called from [org.thoughtcrime.securesms.ApplicationContext.onCreate].
 *
 * The engines are only activated once the admin-approval gate ([MasterIntegration.checkAdminApproval])
 * has passed, enforcing the "pending admin approval" contract.
 */
object REDCore {

  private const val TAG = "RED"

  // System A: 4K VoIP Integration
  @Volatile
  private var voipEngine: UltraHDCall? = null

  // System B: PSTN / Dumin Integration (kept strictly isolated from WebRTC)
  @Volatile
  private var pstnEngine: DuminManager? = null

  // System C: Messaging (Guaranteed Delivery Engine)
  @Volatile
  private var deliveryEngine: GuaranteedDelivery? = null

  /**
   * Called once from the Application's [android.app.Application.onCreate]. Safe to call multiple
   * times; subsequent calls are no-ops once initialized.
   */
  @JvmOverloads
  fun initializeEverything(context: Context? = null) {
    // Always publish the local endpoint so any component can read it.
    REDInitialization.initialize()

    // Require admin approval before activating any engine.
    if (context == null || !MasterIntegration.checkAdminApproval(context)) {
      Log.i(TAG, "Initialization deferred: account not yet approved by admin.")
      return
    }

    if (voipEngine != null) {
      return
    }
    voipEngine = UltraHDCall(codec = "AV1", resolution = "4K")
    pstnEngine = DuminManager(org.thoughtcrime.securesms.dependencies.DevelopedServerConfig.DUMIN_GATEWAY_URL)
    deliveryEngine = GuaranteedDelivery(retryStrategy = "ExponentialBackoff")

    runCatching { deliveryEngine?.start() }.onFailure { Log.w(TAG, "delivery start failed", it) }
    runCatching { voipEngine?.setup() }.onFailure { Log.w(TAG, "voip setup failed", it) }
    runCatching { pstnEngine?.connect() }.onFailure { Log.w(TAG, "pstn connect failed", it) }

    Log.i(TAG, "All RED systems online (VoIP 4K/AV1, PSTN/Dumin, Guaranteed Delivery).")
  }

  /** Clear user-scoped RED engines when the RED account logs out. */
  fun reset() {
    deliveryEngine = null
    pstnEngine = null
    voipEngine = null
    Log.i(TAG, "RED engines reset")
  }

  fun voipEngine(): UltraHDCall? = voipEngine
  fun pstnEngine(): DuminManager? = pstnEngine
  fun deliveryEngine(): GuaranteedDelivery? = deliveryEngine
}
