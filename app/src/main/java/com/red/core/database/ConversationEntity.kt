package com.red.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val peerId: String,
    val peerName: String,
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val messageCount: Long = 0L,
    val unreadCount: Long = 0L,
    val isOnline: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false
)

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(conversations: List<ConversationEntity>)

    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE conversationId = :id LIMIT 1")
    suspend fun getConversation(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE peerName LIKE '%' || :query || '%' ORDER BY isPinned DESC, lastTimestamp DESC")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Query("UPDATE conversations SET unreadCount = :count WHERE conversationId = :id")
    suspend fun updateUnreadCount(id: String, count: Long)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE conversationId = :id")
    suspend fun updatePinned(id: String, pinned: Boolean)

    @Query("UPDATE conversations SET isMuted = :muted WHERE conversationId = :id")
    suspend fun updateMuted(id: String, muted: Boolean)

    @Query("UPDATE conversations SET isOnline = :online WHERE peerId = :peerId")
    suspend fun updateOnlineStatus(peerId: String, online: Boolean)

    @Query("DELETE FROM conversations WHERE conversationId = :id")
    suspend fun deleteConversation(id: String)

    @Query("SELECT SUM(unreadCount) FROM conversations")
    suspend fun totalUnreadCount(): Long?
}
