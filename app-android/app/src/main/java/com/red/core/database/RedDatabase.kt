package com.red.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.red.core.delivery.MessageDao
import com.red.core.delivery.MessageEntity

/**
 * Local Room database. Holds the on-device message store (for offline browsing and outbound queue)
 * and the story cache used by [com.red.core.workers.StoryCleanupWorker].
 */
@Database(
  entities = [MessageEntity::class, StoryEntity::class],
  version = 1,
  exportSchema = false
)
abstract class RedDatabase : RoomDatabase() {
  abstract fun messageDao(): MessageDao
  abstract fun storyDao(): StoryDao
}
