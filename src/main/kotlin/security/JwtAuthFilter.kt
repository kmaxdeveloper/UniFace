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
        val path = request.servletPath

        // 1. WebSocket endpointlarini tekshiruvdan butunlay o'tkazib yuboramiz.
        // Chunki SecurityConfig da .permitAll() qilganmiz, filter bu yerda xalaqit bermasligi kerak.
        if (path.startsWith("/ws-attendance")) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")
        var token: String? = null

        // 2. Tokenni Headerdan yoki URL parametridan qidiramiz
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7)
        } else {
            // SockJS uchun: /ws-attendance/info?token=...
            token = request.getParameter("token")
        }

        // 3. Agar token topilsa, autentifikatsiyani amalga oshiramiz
        if (token != null) {
            try {
                if (jwtUtils.validateToken(token)) {
                    val username = jwtUtils.getUsernameFromToken(token)

                    if (username != null && SecurityContextHolder.getContext().authentication == null) {
                        val userDetails = userDetailsService.loadUserByUsername(username)

                        val authentication = UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            } catch (e: Exception) {
                // Xato bo'lsa contextni tozalaymiz, lekin so'rovni to'xtatmaymiz (filterChain davom etadi)
                println("JWT Authentication Error: ${e.message}")
                SecurityContextHolder.clearContext()
            }
        }

        // 4. Zanjirni davom ettiramiz
        filterChain.doFilter(request, response)
    }
}