package com.red.websocket

import com.red.auth.UserRepository
import com.red.auth.UserStatus
import com.red.config.JwtService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.server.HandshakeInterceptor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Tracks live WebSocket sessions per user and exposes the authenticated principal to handlers.
 * Also mirrors online status into Redis so any backend instance can answer presence queries.
 */
@Service
class PresenceService(
  private val redis: StringRedisTemplate,
  private val userRepository: UserRepository? = null
) {
  private val sessions: MutableMap<String, WebSocketSession> = ConcurrentHashMap()

  fun register(userId: String, session: WebSocketSession) {
    sessions[userId]?.let { runCatching { if (it.isOpen) it.close() } }
    sessions[userId] = session
    redis.opsForValue().set("presence:$userId", "online", 60, TimeUnit.SECONDS)
    // Update last seen
    try {
      userRepository?.findById(userId)?.ifPresent { user ->
        user.lastSeenAt = System.currentTimeMillis()
        userRepository.save(user)
      }
    } catch (_: Exception) { /* best effort */ }
  }

  fun unregister(userId: String, session: WebSocketSession) {
    sessions.remove(userId, session)
    redis.delete("presence:$userId")
    // Update last seen
    try {
      userRepository?.findById(userId)?.ifPresent { user ->
        user.lastSeenAt = System.currentTimeMillis()
        userRepository.save(user)
      }
    } catch (_: Exception) { /* best effort */ }
  }

  fun sessionFor(userId: String): WebSocketSession? = sessions[userId]

  fun onlineUserCount(): Int = sessions.size

  fun isOnline(userId: String): Boolean = sessions.containsKey(userId)

  fun onlineUserIds(): Set<String> = sessions.keys.toSet()

  /**
   * Broadcasts a message to all connected users (e.g., for system announcements).
   */
  fun broadcast(message: String) {
    val textMessage = TextMessage(message)
    sessions.values.forEach { session ->
      if (session.isOpen) {
        runCatching { session.sendMessage(textMessage) }
      }
    }
  }
}

/**
 * Authenticates the WebSocket handshake via a `?token=<jwt>` query parameter and stores the
 * resolved userId in the session attributes for downstream handlers.
 */
class JwtHandshakeInterceptor(
  private val jwtService: JwtService
) : HandshakeInterceptor {

  override fun beforeHandshake(
    request: ServerHttpRequest,
    response: ServerHttpResponse,
    wsHandler: org.springframework.web.socket.WebSocketHandler,
    attributes: MutableMap<String, Any>
  ): Boolean {
    val token = request.uri.query
      ?.split("&")
      ?.firstOrNull { it.startsWith("token=") }
      ?.removePrefix("token=")
      ?: return false
    val claims = jwtService.parse(token) ?: return false
    attributes["userId"] = claims.subject
    return true
  }

  override fun afterHandshake(
    request: ServerHttpRequest,
    response: ServerHttpResponse,
    wsHandler: org.springframework.web.socket.WebSocketHandler,
    exception: java.lang.Exception?
  ) = Unit
}
