package com.red.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer

@Configuration
class RedisConfig {

  /**
   * Pub/Sub container for typing indicators and presence fan-out. Multiple backend instances
   * all subscribe so indicators cross process boundaries.
   */
  @Bean
  fun redisContainer(connectionFactory: RedisConnectionFactory): RedisMessageListenerContainer =
    RedisMessageListenerContainer().apply {
      setConnectionFactory(connectionFactory)
      addMessageListener({ message, _ ->
        // Forwarded to local WebSocket recipients by the TypingService; kept generic here.
        println("[redis] pub/sub message: ${String(message.body)}")
      }, PatternTopic("chat:typing:*"))
    }

  @Bean
  fun stringRedisTemplate(factory: RedisConnectionFactory): StringRedisTemplate =
    StringRedisTemplate(factory)
}
