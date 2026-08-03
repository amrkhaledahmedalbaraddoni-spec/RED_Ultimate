package com.red.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val email: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false
)

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(contacts: List<ContactEntity>)

    @Query("SELECT * FROM contacts ORDER BY fullName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE fullName LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY fullName ASC")
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId LIMIT 1")
    suspend fun getContact(userId: String): ContactEntity?

    @Query("DELETE FROM contacts WHERE userId = :userId")
    suspend fun deleteContact(userId: String)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun count(): Int

    @Query("UPDATE contacts SET isOnline = :online WHERE userId = :userId")
    suspend fun updateOnlineStatus(userId: String, online: Boolean)
}
