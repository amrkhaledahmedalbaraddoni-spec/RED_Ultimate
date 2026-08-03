package com.red

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * RED Sovereign Backend.
 *
 * Single, self-contained Spring Boot service that handles:
 *  - Authentication with admin-approval enforcement (System: Gatekeeper)
 *  - Guaranteed message delivery with de-duplication, sequencing and offline sync (System C)
 *  - Real-time chat over WebSocket
 *  - PSTN/Dumin relay (System B) and VoIP signaling relay (System A)
 *  - Local object storage (MinIO) and live monitoring
 *
 * 100% on-prem. No cloud dependencies at runtime.
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@EnableJpaRepositories(basePackages = ["com.red"])
@EnableMongoRepositories(basePackages = ["com.red"])
class RedBackendApplication

fun main(args: Array<String>) {
  runApplication<RedBackendApplication>(*args)
}
