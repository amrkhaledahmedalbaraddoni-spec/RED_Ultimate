package com.red.server.pstn

import org.asteriskjava.manager.DefaultManagerConnection
import org.asteriskjava.manager.action.OriginateAction
import org.springframework.stereotype.Service

@Service
class PstnManager {
    private val asterisk = DefaultManagerConnection("red-pstn-gateway", "red_admin", "red_secret_123")

    init {
        try {
            asterisk.login()
            println("🔴 RED PSTN: Connected to Asterisk Gateway.")
        } catch (e: Exception) {
            println("⚠️ RED PSTN: Gateway connection failed: ${e.message}")
        }
    }

    fun dialGsm(phoneNumber: String): String {
        val action = OriginateAction().apply {
            channel = "PJSIP/$phoneNumber@dumin-trunk"
            context = "from-internal"
            exten = "s"
            priority = 1
            callerId = "RED SOVEREIGN"
        }
        val response = asterisk.sendAction(action)
        return response.actionId
    }
}
