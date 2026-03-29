package com.uniface

import com.uniface.security.JwtAuthFilter
import com.uniface.security.JwtUtils
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import jakarta.servlet.FilterChain
import org.springframework.security.core.authority.SimpleGrantedAuthority

@ExtendWith(MockKExtension::class)
class JwtAuthFilterTest {

    @MockK
    lateinit var jwtUtils: JwtUtils

    @MockK
    lateinit var userDetailsService: UserDetailsService

    @InjectMockKs
    lateinit var jwtAuthFilter: JwtAuthFilter

    @Test
    fun `should authenticate when valid token is provided`() {
        // Given
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val filterChain = mockk<FilterChain>(relaxed = true)
        val token = "valid_token"
        val username = "komil2001"

        // MUHIM: UserDetails tipida mock yaratamiz
        val userDetails = org.springframework.security.core.userdetails.User
            .withUsername(username)
            .password("password")
            .authorities("ROLE_TEACHER")
            .build()

        request.addHeader("Authorization", "Bearer $token")

        every { jwtUtils.validateToken(token) } returns true
        every { jwtUtils.getUsernameFromToken(token) } returns username

        // Bu yerda UserDetails qaytishini aniq ko'rsatamiz
        every { userDetailsService.loadUserByUsername(username) } returns userDetails

        // When
        jwtAuthFilter.doFilter(request, response, filterChain)

        // Then
        val auth = SecurityContextHolder.getContext().authentication
        assert(auth != null) { "Authentication should not be null" }
        assert(auth?.name == username)

        verify { filterChain.doFilter(request, response) }
    }
}