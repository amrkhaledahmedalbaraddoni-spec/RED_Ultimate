package com.red.config

import com.red.websocket.ChatWebSocketHandler
import com.red.websocket.JwtHandshakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
  private val chatHandler: ChatWebSocketHandler,
  private val jwtService: JwtService
) : WebSocketConfigurer {

  override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
    registry.addHandler(chatHandler, "/ws/chat")
      .addInterceptors(JwtHandshakeInterceptor(jwtService))
      .setAllowedOriginPatterns("*")
  }
}
