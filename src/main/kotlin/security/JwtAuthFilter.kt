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
        // 1. CORS headerlarini va OPTIONS tekshiruvini bu yerdan olib tashladik.
        // Chunki bu ishni SecurityConfig dagi .cors() va permitAll(OPTIONS) bajaradi.

        val authHeader = request.getHeader("Authorization")

        // 2. Token yo'q bo'lsa, zanjirni davom ettirib, chiqib ketamiz
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        try {
            if (jwtUtils.validateToken(token)) {
                val username = jwtUtils.getUsernameFromToken(token)

                if (username != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(username)

                    // DEBUG: Rollar to'g'ri kelayotganini terminalda ko'rish uchun
                    println("User: $username | Roles: ${userDetails.authorities}")

                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (e: Exception) {
            println("JWT Authentication Error: ${e.message}")
            SecurityContextHolder.clearContext()
        }

        // 3. MUHIM: Har qanday holatda ham zanjirni oxirigacha yetkazish shart!
        filterChain.doFilter(request, response)
    }
}