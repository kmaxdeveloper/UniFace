package com.uniface.config

import com.uniface.security.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtAuthFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            // 1. BU YERGA SHU QATORNI QO'SHISH SHART! ✅
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                // 2. OPTIONS so'rovlariga ruxsat berish (Preflight uchun)
                auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                auth.requestMatchers("/ws-attendance/**").permitAll()
                auth.requestMatchers("/api/v1/teacher/generate-qr/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")

                auth.requestMatchers("/api/v1/auth/**").permitAll()
                // Avval aniq endpointlar ✅
                auth.requestMatchers("/api/v1/admin/groups/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                auth.requestMatchers("/api/v1/admin/subjects/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                // Keyin umumiy
                auth.requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                auth.requestMatchers("/api/v1/teacher/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                auth.requestMatchers("/api/v1/face/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                auth.requestMatchers("/api/v1/student/scan/**").permitAll()
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        // MUHIM: Faqat sening React portingga ruxsat
        configuration.allowedOrigins = listOf("http://localhost:5173")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L // Brauzer keshlab olishi uchun

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager
}