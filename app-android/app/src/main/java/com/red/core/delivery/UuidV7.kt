package com.red.core.delivery

import java.security.SecureRandom
import java.util.UUID

/**
 * RFC 9562 UUID version 7 generator (48-bit ms timestamp + version + random). Used as the
 * client-side message id, matching the server's de-duplication scheme.
 */
object UuidV7 {

  private val random: SecureRandom = SecureRandom()

  fun now(): String = generate(System.currentTimeMillis(), random)

  @Synchronized
  fun generate(timestampMillis: Long, random: java.util.Random): String {
    val ts = timestampMillis and 0xFFFFFFFFFFFFL
    var msb = ts shl 16
    msb = msb or (random.nextInt() and 0xFFF).toLong()
    msb = (msb and 0xFFFFFFFFFFFF0FFFL) or 0x7000L

    var lsb = (random.nextInt() and 0x3FFF).toLong() shl 48
    lsb = lsb or (random.nextLong() and 0xFFFFFFFFFFFFL)
    lsb = (lsb and 0x3FFFFFFFFFFFFFFFL) or 0x8000000000000000L

    return UUID(msb, lsb).toString()
  }
}
