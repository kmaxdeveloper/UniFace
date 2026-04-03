package com.uniface.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtUtils: JwtUtils,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        var token: String? = null

        // 1. Tokenni qidirish (Header yoki URL param)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7)
        } else {
            // SockJS uchun URL dan tokenni olamiz
            token = request.getParameter("token")
        }

        // 2. Agar token bo'lsa, uni tekshirib contextga qo'yamiz
        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            try {
                if (jwtUtils.validateToken(token)) {
                    val username = jwtUtils.getUsernameFromToken(token)
                    if (username != null) {
                        val userDetails = userDetailsService.loadUserByUsername(username)
                        val authentication = UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            } catch (e: Exception) {
                // Logga chiqaramiz, lekin filterChainni to'xtatmaymiz
                logger.error("JWT Error: ${e.message}")
            }
        }

        // 3. WebSocket yo'llari uchun SecurityConfig dagi .permitAll() ishlashi kerak
        // Lekin biz yuqorida tokenni o'qib bo'ldik (agar u bo'lsa)
        filterChain.doFilter(request, response)
    }
}