package com.red.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "stories")
data class StoryEntity(
  @PrimaryKey val id: String,
  val ownerId: String,
  val mediaUrl: String,
  val createdAt: Long,
  val expiresAt: Long
)

@Dao
interface StoryDao {
  @Insert
  suspend fun insert(story: StoryEntity)

  @Query("DELETE FROM stories WHERE expiresAt <= :now")
  suspend fun cleanupExpired(now: Long): Int
}
