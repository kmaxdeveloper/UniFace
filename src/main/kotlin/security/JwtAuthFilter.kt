package com.uniface.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
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
        // CORS Headerlarini har doim birinchi bo'lib qo'shamiz
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173")
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, Origin")
        response.setHeader("Access-Control-Allow-Credentials", "true")

        // OPTIONS so'rovi kelsa, shu yerda javobni qaytaramiz
        if ("OPTIONS".equals(request.method, ignoreCase = true)) {
            response.status = HttpServletResponse.SC_OK
            return
        }

        val authHeader = request.getHeader("Authorization")

        // Qolgan JWT tekshirish koding...
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        // 3. Token validatsiyasi va SecurityContext-ni to'ldirish
        try {
            if (jwtUtils.validateToken(token)) {
                val username = jwtUtils.getUsernameFromToken(token)

                // Context bo'shligini ham tekshirib qo'yish yaxshi praktika
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
            // Token xato bo'lsa context-ni tozalaymiz
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}