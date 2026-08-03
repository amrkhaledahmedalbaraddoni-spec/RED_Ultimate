package com.red.pstn

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * PSTN (Dumin/GSM) call controller.
 * Handles dialing, call status, and hangup via the Asterisk/Dumin gateway.
 */
@RestController
@RequestMapping("/api/pstn")
class PstnController(private val pstn: PstnService) {

  data class DialRequest(val number: String, val duminIp: String = "")
  data class HangupRequest(val callId: String)

  @PostMapping("/dial")
  fun dial(@RequestBody request: DialRequest): PstnService.CallResponse =
    pstn.dial(request.number, request.duminIp)

  @GetMapping("/call/{callId}")
  fun getCallStatus(@PathVariable callId: String): PstnService.CallResponse =
    pstn.getCallStatus(callId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Call not found")

  @PostMapping("/hangup")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun hangup(@RequestBody request: HangupRequest) {
    pstn.hangup(request.callId)
  }

  @GetMapping("/sim")
  fun sim(): Map<String, String> = pstn.simStatus()
}
