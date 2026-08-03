package com.red.pstn

import com.red.config.DuminProperties
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * System B: PSTN bridge. Commands the local Dumin/GSM gateway (or Asterisk AMI) to place a call.
 * Strictly isolated from System A (WebRTC) — this service only deals with PSTN signaling.
 *
 * Phone numbers are URL-encoded to prevent parameter injection and all errors are surfaced as a
 * typed result rather than exceptions.
 */
@Service
class PstnService(props: DuminProperties) {

  data class Result(val ok: Boolean, val callId: String?, val message: String)

  private val baseHttpUrl: String = props.baseUrl.removeSuffix("/")
  private val apiToken: String = props.apiToken
  private val client: RestClient = RestClient.create()

  fun dial(phoneNumber: String): Result {
    require(phoneNumber.matches(Regex("^[0-9+]{4,20}$"))) { "Invalid phone number" }
    val encoded = URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8)
    val url = "$baseHttpUrl/api/call/dial?number=$encoded"
    return try {
      val body = client.post()
        .uri(url)
        .header("X-Dumin-Token", apiToken)
        .retrieve()
        .body(Map::class.java)
      Result(ok = true, callId = body?.get("call_id") as? String, message = "Dialing")
    } catch (e: Exception) {
      Result(ok = false, callId = null, message = e.message ?: "Gateway error")
    }
  }

  fun simStatus(): Map<String, Any?> = try {
    client.get().uri("$baseHttpUrl/api/sim/status").retrieve().body(Map::class.java) ?: emptyMap()
  } catch (e: Exception) {
    mapOf("status" to "UNREACHABLE", "error" to (e.message ?: "unknown"))
  }
}
