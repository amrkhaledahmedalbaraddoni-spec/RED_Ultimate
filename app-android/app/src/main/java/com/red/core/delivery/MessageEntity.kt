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
  val status: MessageStatus = MessageStatus.SENDING,
  val type: String = "TEXT"  // TEXT, IMAGE, VIDEO, FILE, VOICE
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

  @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
  suspend fun getMessage(id: String): MessageEntity?

  @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
  suspend fun getMessageById(id: String): MessageEntity?

  @Query("DELETE FROM messages WHERE id = :id")
  suspend fun deleteMessage(id: String)

  @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND senderId != :myUserId AND status != :readStatus")
  suspend fun getUnreadCount(conversationId: String, myUserId: String, readStatus: MessageStatus = MessageStatus.READ): Int

  @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND (type = 'IMAGE' OR type = 'VIDEO' OR type = 'FILE') ORDER BY timestamp DESC")
  fun getMediaMessages(conversationId: String): Flow<List<MessageEntity>>
}
