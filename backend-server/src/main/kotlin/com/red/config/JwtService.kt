package com.red.config

import com.red.auth.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

/**
 * Stateless JWT provider. Signs tokens with HS256 using [JwtProperties.secret].
 */
@Service
class JwtService(props: JwtProperties) {

  private val key: SecretKey = Keys.hmacShaKeyFor(props.secret.toByteArray(StandardCharsets.UTF_8))
  private val ttlMillis: Long = props.ttlMinutes * 60_000L

  fun issue(userId: String, email: String, role: UserRole): String {
    val now = Date()
    return Jwts.builder()
      .subject(userId)
      .claim("email", email)
      .claim("role", role.name)
      .issuedAt(now)
      .expiration(Date(now.time + ttlMillis))
      .signWith(key)
      .compact()
  }

  fun parse(token: String): Claims? = try {
    Jwts.parser()
      .verifyWith(key)
      .build()
      .parseSignedClaims(token)
      .payload
  } catch (e: JwtException) {
    null
  } catch (e: IllegalArgumentException) {
    null
  }
}
