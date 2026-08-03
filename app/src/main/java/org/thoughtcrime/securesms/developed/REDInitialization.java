package org.thoughtcrime.securesms.developed;

import android.util.Log;

import org.thoughtcrime.securesms.dependencies.DevelopedServerConfig;

/**
 * RED Initialization System.
 *
 * Binds the app to the LOCAL sovereign server. This is intentionally lightweight and side-effect
 * free: it only configures JVM system properties that downstream RED components may consult.
 *
 * Note: This does NOT disable Signal's own networking. Redirecting Signal's real service network
 * requires replacing {@code SignalServiceConfiguration}; that wiring is out of scope for this
 * helper and is handled by the dedicated backend in {@code backend-server}.
 */
public final class REDInitialization {

  private REDInitialization() {
  }

  /**
   * @return the local sovereign server URL the app should talk to.
   */
  public static String getLocalServerUrl() {
    return DevelopedServerConfig.SIGNAL_URL;
  }

  /**
   * Publishes the local server URL as a system property so any RED component that opts-in can
   * read a single source of truth. Idempotent and thread-safe.
   */
  public static void initialize() {
    System.setProperty("red.service.url", getLocalServerUrl());
    Log.i("RED", "local server endpoint = " + getLocalServerUrl());
  }
}
