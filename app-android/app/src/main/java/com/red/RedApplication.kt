package com.red

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.red.core.delivery.NotificationHelper
import com.red.core.security.SessionManager
import com.red.core.workers.StoryCleanupWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * RED application entry point. Enables Hilt across the app and registers WorkManager with the
 * Hilt-aware worker factory so [StoryCleanupWorker] can inject its dependencies.
 */
@HiltAndroidApp
class RedApplication : Application(), Configuration.Provider {

  @Inject
  lateinit var workerFactory: HiltWorkerFactory

  @Inject
  lateinit var sessionManager: SessionManager

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()

  override fun onCreate() {
    super.onCreate()
    // Create notification channels for Android O+
    NotificationHelper.createChannels(this)
    // Enqueue periodic story cleanup
    StoryCleanupWorker.enqueue(this)
    // Initialize encryption key
    runCatching { sessionManager.getEncryptionKey() }
  }
}
