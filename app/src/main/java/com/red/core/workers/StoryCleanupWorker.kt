package com.red.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.red.core.database.RedDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodically removes expired (24h) stories from the local cache, mirroring the server-side
 * auto-delete behaviour.
 */
@HiltWorker
class StoryCleanupWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParams: WorkerParameters,
  private val database: RedDatabase
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    return runCatching {
      database.storyDao().cleanupExpired(System.currentTimeMillis())
      Result.success()
    }.getOrElse { Result.retry() }
  }

  companion object {
    fun enqueue(context: Context) {
      val request = PeriodicWorkRequestBuilder<StoryCleanupWorker>(15, TimeUnit.MINUTES).build()
      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "StoryCleanup",
        ExistingPeriodicWorkPolicy.KEEP,
        request
      )
    }
  }
}
