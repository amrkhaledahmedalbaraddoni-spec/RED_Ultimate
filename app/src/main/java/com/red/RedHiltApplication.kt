package com.red

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.red.core.security.SessionManager
import dagger.hilt.android.HiltAndroidApp
import org.thoughtcrime.securesms.ApplicationContext
import javax.inject.Inject

/**
 * Hilt application entry point for the merged RED/Signal APK.
 *
 * AGP 9's built-in Kotlin support makes the KGP `kapt` plugin unavailable, and Hilt's KSP
 * processor only processes Kotlin sources — it cannot see `@HiltAndroidApp` or `@Inject`
 * on Java classes. Signal's [ApplicationContext] is a Java class, so the Hilt wiring lives
 * here in Kotlin and simply extends [ApplicationContext], keeping every Signal
 * initialization hook in the same class hierarchy, process, and APK.
 *
 * The manifest points `android:name` at this class; `ApplicationContext.getInstance()`
 * still returns this instance because it *is* an ApplicationContext.
 */
@HiltAndroidApp
class RedHiltApplication : ApplicationContext(), Configuration.Provider {

  /** Hilt bridges the merged RED workers into the existing Signal application. */
  @Inject lateinit var redWorkerFactory: HiltWorkerFactory

  @Inject lateinit var redSessionManager: SessionManager

  override fun getWorkManagerConfiguration(): Configuration =
    Configuration.Builder()
      .setWorkerFactory(redWorkerFactory)
      .build()

  override fun onCreate() {
    // Hilt injects fields in the generated base class *before* this runs.
    super.onCreate()
    // Warm up the RED local-encryption key so the first message write is fast.
    runCatching { redSessionManager.getEncryptionKey() }
  }
}
