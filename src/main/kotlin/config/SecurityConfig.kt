package com.uniface.config

import com.uniface.security.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
            // 1. CORS har doim birinchi bo'lishi shart!
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }

            .headers { it.frameOptions { f -> f.disable() } }

            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                // OPTIONS (Preflight) so'rovlarini hammasiga ruxsat
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // WebSocket va SockJS /info so'rovlari uchun yo'lni ochamiz
                auth.requestMatchers("/ws-attendance/**").permitAll()

                // Auth va ochiq endpointlar
                auth.requestMatchers("/api/v1/auth/**").permitAll()
                auth.requestMatchers("/api/v1/student/**").hasAnyAuthority("ROLE_STUDENT", "ROLE_ADMIN")

                // Qolgan rollar bo'yicha cheklovlar
                auth.requestMatchers("/api/v1/teacher/generate-qr/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                auth.requestMatchers("/api/v1/admin/groups/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                auth.requestMatchers("/api/v1/admin/subjects/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                auth.requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                auth.requestMatchers("/api/v1/teacher/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                auth.requestMatchers("/api/v1/face/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")

                auth.anyRequest().authenticated()
            }
            // 2. JWT filtri WebSocket yo'llariga xalaqit bermasligi kerak
            // (JwtAuthFilter ichida path.startsWith("/ws-attendance") tekshiruvi bo'lishi shart)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // MUHIM: Ham HTTP, ham HTTPS protokollarini va subdomenlarni qamrab olamiz
        configuration.allowedOriginPatterns = listOf(
            "http://localhost:5173",
            "https://timora.uz",
            "https://www.timora.uz",
            "https://api.timora.uz",
            "http://timora.uz",      // Ba'zan localhost testlar uchun kerak
            "http://api.timora.uz",
            "https://*.timora.uz", // Barcha subdomenlar uchun (masalan neura.timora.uz)
            "http://api.timora.uz*",
            "http://timora.uz*"
        )

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")

        // Hamma kerakli headerlarga ruxsat beramiz
        configuration.allowedHeaders = listOf("*")

        // SockJS session/cookie ishlashi uchun true bo'lishi shart
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        // Hamma endpointlar uchun ushbu CORS sozlansin
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager
}