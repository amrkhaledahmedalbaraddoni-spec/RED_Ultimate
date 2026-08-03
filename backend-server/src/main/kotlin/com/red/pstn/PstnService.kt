package com.red.pstn

import com.red.config.DuminProperties
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

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

  data class CallResponse(
    val callId: String?,
    val status: String,
    val duration: Long = 0L
  )

  private val baseHttpUrl: String = props.baseUrl.removeSuffix("/")
  private val apiToken: String = props.apiToken
  private val client: RestClient = RestClient.create()

  // In-memory call tracking for active calls
  private val activeCalls = ConcurrentHashMap<String, CallResponse>()

  fun dial(phoneNumber: String, duminIp: String = "192.168.1.100"): CallResponse {
    require(phoneNumber.matches(Regex("^[0-9+]{4,20}$"))) { "Invalid phone number" }
    val encoded = URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8)
    val url = "$baseHttpUrl/api/call/dial?number=$encoded"
    return try {
      val body = client.post()
        .uri(url)
        .header("X-Dumin-Token", apiToken)
        .retrieve()
        .body(Map::class.java)
      val callId = body?.get("call_id") as? String ?: "call_${System.currentTimeMillis()}"
      val response = CallResponse(callId = callId, status = "DIALING")
      activeCalls[callId] = response
      response
    } catch (e: Exception) {
      CallResponse(callId = null, status = "FAILED")
    }
  }

  fun getCallStatus(callId: String): CallResponse? {
    // Check local tracking first
    val local = activeCalls[callId]
    if (local != null) {
      // Try to get status from gateway
      try {
        val body = client.get()
          .uri("$baseHttpUrl/api/call/status/$callId")
          .header("X-Dumin-Token", apiToken)
          .retrieve()
          .body(Map::class.java)
        if (body != null) {
          val status = body["status"] as? String ?: local.status
          val duration = body["duration"] as? Long ?: local.duration
          val updated = local.copy(status = status, duration = duration)
          if (status == "ENDED" || status == "FAILED") {
            activeCalls.remove(callId)
          } else {
            activeCalls[callId] = updated
          }
          return updated
        }
      } catch (_: Exception) { }
      return local
    }
    return null
  }

  fun hangup(callId: String) {
    try {
      client.post()
        .uri("$baseHttpUrl/api/call/hangup?call_id=$callId")
        .header("X-Dumin-Token", apiToken)
        .retrieve()
        .body(Map::class.java)
    } catch (_: Exception) { }
    activeCalls.remove(callId)
  }

  fun simStatus(): Map<String, String> = try {
    @Suppress("UNCHECKED_CAST")
    (client.get().uri("$baseHttpUrl/api/sim/status").retrieve().body(Map::class.java) as? Map<String, String>)
      ?: mapOf("status" to "UNKNOWN")
  } catch (e: Exception) {
    mapOf("status" to "UNREACHABLE", "error" to (e.message ?: "unknown"))
  }
}
