package com.red.core.security

import okhttp3.CertificatePinner as OkHttpCertificatePinner
import org.thoughtcrime.securesms.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional certificate pinning for the RED endpoint.
 *
 * Pins are supplied at build time with `-Pred.certificate.pins=sha256/...,sha256/...`.
 * Local/private-network hosts intentionally skip pinning because they commonly use a local CA.
 * Production builds should provide at least two pins (current and backup certificate) during
 * certificate rotation.
 */
@Singleton
class CertificatePinner @Inject constructor() {

    private val productionPins = BuildConfig.RED_CERTIFICATE_PINS.toList()

    fun buildCertificatePinner(serverHost: String): OkHttpCertificatePinner {
        val builder = OkHttpCertificatePinner.Builder()
        if (!isLocalServer(serverHost)) {
            productionPins.forEach { pin -> builder.add(serverHost, pin) }
        }
        return builder.build()
    }

    private fun isLocalServer(host: String): Boolean {
        return host.startsWith("192.168.") ||
            host.startsWith("10.") ||
            host.startsWith("172.") ||
            host == "localhost" ||
            host == "127.0.0.1"
    }
}
