package com.red.security

/**
 * Redacts sensitive data (IPs, emails, Bearer tokens) from log messages.
 */
object LogScrubber {
  private val IPV4 = Regex("""\b\d{1,3}(\.\d{1,3}){3}\b""")
  private val EMAIL = Regex("""\b[\w.+-]+@[\w-]+\.[\w.-]+\b""")
  private val BEARER = Regex("""Bearer\s+\S+""", RegexOption.IGNORE_CASE)

  fun scrub(input: String): String = input
    .replace(BEARER, "Bearer [redacted]")
    .replace(EMAIL, "[email]")
    .replace(IPV4, "[ip]")
}
