package com.red.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
  private val jwtAuthFilter: JwtAuthenticationFilter
) {

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .csrf { it.disable() } // stateless JWT API; CSRF not applicable
      .cors { it.configurationSource(corsConfigurationSource()) }
      .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
      .authorizeHttpRequests { auth ->
        auth
          .requestMatchers(
            "/api/auth/register",
            "/api/auth/login",
            "/actuator/health",
            "/actuator/info",
            "/ws/**"
          ).permitAll()
          .requestMatchers("/api/admin/**").hasRole("ADMIN")
          .anyRequest().authenticated()
      }
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
    return http.build()
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

  @Bean
  fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
    config.authenticationManager

  @Bean
  fun corsConfigurationSource(): CorsConfigurationSource {
    val config = CorsConfiguration().apply {
      allowedOriginPatterns = listOf("*")
      allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
      allowedHeaders = listOf("*")
      allowCredentials = true
    }
    return UrlBasedCorsConfigurationSource().apply {
      registerCorsConfiguration("/**", config)
    }
  }
}

/**
 * JWT signing secret and token lifetime. In production, set RED_JWT_SECRET to a long random value.
 */
@ConfigurationProperties(prefix = "red.jwt")
data class JwtProperties(
  var secret: String = "change-me-red-sovereign-secret-key-please-rotate-32bytes-min",
  var ttlMinutes: Long = 60 * 24L
)

@ConfigurationProperties(prefix = "red.dumin")
data class DuminProperties(
  var baseUrl: String = "http://192.168.1.100:5060",
  var apiToken: String = ""
)

@ConfigurationProperties(prefix = "red.storage")
data class StorageProperties(
  var endpoint: String = "http://minio:9000",
  var accessKey: String = "redadmin",
  var secretKey: String = "redsecret123",
  var bucket: String = "red-media"
)
