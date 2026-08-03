package com.red.config

import com.red.auth.UserRole
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Extracts and validates the Bearer JWT, populating the SecurityContext with the user id and role.
 */
@Component
class JwtAuthenticationFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    val header = request.getHeader("Authorization")
    if (header != null && header.startsWith("Bearer ")) {
      val token = header.removePrefix("Bearer ").trim()
      jwtService.parse(token)?.let { claims ->
        val role = claims.get("role", String::class.java) ?: UserRole.USER.name
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        val auth = UsernamePasswordAuthenticationToken(claims.subject, null, authorities)
        SecurityContextHolder.getContext().authentication = auth
      }
    }
    filterChain.doFilter(request, response)
  }
}
