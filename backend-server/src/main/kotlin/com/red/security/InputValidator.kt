package com.red.security

import org.springframework.stereotype.Component

/**
 * Input validation utility — prevents XSS, injection, and malformed data.
 */
@Component
class InputValidator {

    // Maximum lengths
    private const val MAX_NAME_LENGTH = 100
    private const val MAX_EMAIL_LENGTH = 254
    private const val MAX_PHONE_LENGTH = 20
    private const val MAX_MESSAGE_LENGTH = 65536
    private const val MAX_GROUP_NAME_LENGTH = 100
    private const val MAX_GROUP_DESCRIPTION_LENGTH = 500
    private const val MIN_PASSWORD_LENGTH = 8

    // Regex patterns
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val phonePattern = Regex("^\\+?[0-9]{7,15}$")
    private val safeTextPattern = Regex("^[\\p{L}\\p{N}\\s\\p{P}\\p{Sm}]+$")
    private val xssPattern = Regex("<script|javascript:|on\\w+\\s*=|<iframe|<object|<embed", RegexOption.IGNORE_CASE)

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )

    fun validateName(name: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (name.isBlank()) errors.add("Name cannot be blank")
        if (name.length > MAX_NAME_LENGTH) errors.add("Name too long (max $MAX_NAME_LENGTH)")
        if (xssPattern.containsMatchIn(name)) errors.add("Name contains invalid characters")
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateEmail(email: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (email.isBlank()) errors.add("Email cannot be blank")
        if (email.length > MAX_EMAIL_LENGTH) errors.add("Email too long (max $MAX_EMAIL_LENGTH)")
        if (!emailPattern.matches(email)) errors.add("Invalid email format")
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validatePhone(phone: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (phone.isNotBlank() && !phonePattern.matches(phone)) {
            errors.add("Invalid phone number format")
        }
        if (phone.length > MAX_PHONE_LENGTH) errors.add("Phone number too long")
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validatePassword(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (password.length < MIN_PASSWORD_LENGTH) errors.add("Password must be at least $MIN_PASSWORD_LENGTH characters")
        if (password.length > 128) errors.add("Password too long")
        if (!password.any { it.isDigit() }) errors.add("Password must contain at least one digit")
        if (!password.any { it.isUpperCase() }) errors.add("Password must contain at least one uppercase letter")
        if (!password.any { it.isLowerCase() }) errors.add("Password must contain at least one lowercase letter")
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateMessagePayload(payload: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (payload.isBlank()) errors.add("Message cannot be blank")
        if (payload.length > MAX_MESSAGE_LENGTH) errors.add("Message too long (max $MAX_MESSAGE_LENGTH)")
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateGroupName(name: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (name.isBlank()) errors.add("Group name cannot be blank")
        if (name.length > MAX_GROUP_NAME_LENGTH) errors.add("Group name too long (max $MAX_GROUP_NAME_LENGTH)")
        if (xssPattern.containsMatchIn(name)) errors.add("Group name contains invalid characters")
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateGroupDescription(description: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (description.length > MAX_GROUP_DESCRIPTION_LENGTH) errors.add("Description too long (max $MAX_GROUP_DESCRIPTION_LENGTH)")
        if (xssPattern.containsMatchIn(description)) errors.add("Description contains invalid characters")
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun sanitize(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .trim()
    }
}
