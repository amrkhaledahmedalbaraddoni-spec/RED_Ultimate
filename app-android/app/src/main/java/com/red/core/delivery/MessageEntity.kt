package com.red.core.delivery

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages")
data class MessageEntity(
  @PrimaryKey val id: String,
  val conversationId: String,
  val senderId: String,
  val receiverId: String,
  val payload: String,
  val timestamp: Long,
  val sequenceNumber: Long = 0,
  val status: MessageStatus = MessageStatus.SENDING
)

@Dao
interface MessageDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(message: MessageEntity)

  @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
  fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

  @Query("UPDATE messages SET status = :status WHERE id = :id")
  suspend fun updateStatus(id: String, status: MessageStatus)

  @Query("SELECT MAX(sequenceNumber) FROM messages WHERE conversationId = :conversationId")
  suspend fun lastSequence(conversationId: String): Long?
}
