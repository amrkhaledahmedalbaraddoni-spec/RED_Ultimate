package org.thoughtcrime.securesms.developed.delivery

import android.util.Log
import java.security.SecureRandom
import java.util.UUID

/**
 * System C: Guaranteed Delivery Engine.
 *
 * Generates time-ordered, monotonically-sortable message identifiers that are also unique across
 * distributed senders, which is the property the delivery layer relies on for de-duplication and
 * offline sync ordering.
 *
 * [generateMsgId] produces a spec-compliant RFC 9562 **UUID version 7**: a 48-bit Unix-millisecond
 * timestamp followed by 12 bits of random "rand_a" and 62 bits of random "rand_b", with the
 * version and variant bits set correctly. This is a real UUIDv7 — not a timestamp-prefixed UUIDv4.
 */
class GuaranteedDelivery(val retryStrategy: String) {

  private val random = SecureRandom()

  fun start() {
    Log.i("RED", "Guaranteed Delivery Engine started (retry=$retryStrategy)")
  }

  /**
   * @return a fresh RFC 9562 UUID version 7 as a canonical 36-character lower-hex string.
   */
  fun generateMsgId(): String = generateUuidV7(random)

  companion object {
    private const val TAG = "RED"

    /**
     * Constructs an RFC 9562 UUIDv7 from [timestampMillis] and [random]. Exposed for testing.
     */
    @JvmStatic
    @JvmOverloads
    fun generateUuidV7(random: java.util.Random = SecureRandom(), timestampMillis: Long = System.currentTimeMillis()): String {
      val mostSigBits = LongArray(1)
      val leastSigBits = LongArray(1)

      // msb = unix_ts_ms (48 bits, bits 63..16) | ver(0111) | rand_a(12 bits)
      val ts = timestampMillis and 0xFFFFFFFFFFFFL
      var msb = ts shl 16
      val randA = (random.nextInt() and 0xFFF).toLong()
      msb = msb or (randA and 0xFFFFL)
      msb = (msb and 0xFFFFFFFFFFFF0FFFL) or 0x7000L // set version 7

      // lsb = variant(10) | rand_b(62 bits)
      val randBHigh = (random.nextInt() and 0x3FFF).toLong()
      var lsb = randBHigh shl 48
      lsb = lsb or (random.nextLong() and 0xFFFFFFFFFFFFL)
      lsb = (lsb and 0x3FFFFFFFFFFFFFFFL) or 0x8000000000000000L // set variant 10

      return UUID(msb, lsb).toString()
    }
  }
}
