package com.red.core.security

import okhttp3.CertificatePinner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Certificate pinning configuration for RED Sovereign.
 *
 * In production, pin the SHA-256 hashes of your server's certificate chain.
 * For development/local servers, certificate pinning is disabled.
 *
 * To get the SHA-256 hash of a certificate:
 *   openssl s_client -connect your-server:443 | openssl x509 -pubkey | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
 */
@Singleton
class CertificatePinner @Inject constructor() {

    companion object {
        // Production pins — replace with your actual certificate hashes
        private val PRODUCTION_PINS = listOf(
            // "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
            // Add your server's certificate SHA-256 pin here
        )

        // Local development server — no pinning
        private const val LOCAL_SERVER = "192.168.1.50"
    }

    fun buildCertificatePinner(serverHost: String): CertificatePinner {
        val builder = CertificatePinner.Builder()

        // Only apply pinning for non-local servers
        if (!isLocalServer(serverHost) && PRODUCTION_PINS.isNotEmpty()) {
            for (pin in PRODUCTION_PINS) {
                builder.add(serverHost, pin)
            }
        }

        return builder.build()
    }

    private fun isLocalServer(host: String): Boolean {
        return host.startsWith("192.168.") ||
               host.startsWith("10.") ||
               host.startsWith("172.16.") ||
               host.startsWith("172.17.") ||
               host.startsWith("172.18.") ||
               host.startsWith("172.19.") ||
               host.startsWith("172.2") ||
               host.startsWith("172.3") ||
               host == "localhost" ||
               host == "127.0.0.1"
    }
}
