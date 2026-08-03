package com.red.contacts

import com.red.auth.UserEntity
import com.red.auth.UserRepository
import com.red.auth.UserView
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * Contact management controller.
 * Allows users to maintain a personal contact list, add/remove contacts,
 * and sync phone contacts.
 */
@RestController
@RequestMapping("/api/contacts")
class ContactController(
    private val contactRepository: ContactRepository,
    private val userRepository: UserRepository
) {

    @GetMapping
    fun getContacts(authentication: Authentication): List<UserView> {
        val contacts = contactRepository.getContactIds(authentication.name)
        return userRepository.findAllById(contacts).map { UserView.from(it) }
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addContact(authentication: Authentication, @RequestBody request: Map<String, String>) {
        val email = request["email"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "email required")
        val targetUser = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        contactRepository.addContact(authentication.name, targetUser.id)
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeContact(authentication: Authentication, @PathVariable userId: String) {
        contactRepository.removeContact(authentication.name, userId)
    }

    @GetMapping("/sync")
    fun syncPhoneContacts(authentication: Authentication, @RequestParam phones: String): List<UserView> {
        val phoneList = phones.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val found = userRepository.findByPhoneNumbers(phoneList)
        return found.map { UserView.from(it) }
    }
}
