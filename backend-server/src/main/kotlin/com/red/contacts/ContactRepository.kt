package com.red.contacts

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

/**
 * Contact repository using Redis.
 * Stores user contact lists as Redis sets for fast lookup.
 */
@Repository
class ContactRepository(
    private val redis: StringRedisTemplate
) {

    private fun key(userId: String) = "contacts:$userId"

    /** Get all contact IDs for a user. */
    fun getContactIds(userId: String): List<String> {
        return redis.opsForSet().members(key(userId))?.toList() ?: emptyList()
    }

    /** Add a contact to the user's contact list. */
    fun addContact(userId: String, contactId: String) {
        redis.opsForSet().add(key(userId), contactId)
    }

    /** Remove a contact from the user's contact list. */
    fun removeContact(userId: String, contactId: String) {
        redis.opsForSet().remove(key(userId), contactId)
    }

    /** Check if a user is in another user's contact list. */
    fun isContact(userId: String, contactId: String): Boolean {
        return redis.opsForSet().isMember(key(userId), contactId) == true
    }
}
