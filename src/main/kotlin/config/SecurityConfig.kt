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
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                // 1. Preflight so'rovlar har doim ochiq bo'lishi shart
                auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                // 2. To'liq ochiq endpointlar
                auth.requestMatchers(
                    "/api/v1/auth/**",
                    "/ws-attendance/**",
                    "/api/v1/student/scan/**"
                ).permitAll()

                // 3. TEACHER va ADMIN kirishi mumkin bo'lgan MAXSUS endpointlar (Bular ADMIN/** dan tepada turishi shart!)
                auth.requestMatchers(
                    "/api/v1/admin/groups/**",
                    "/api/v1/admin/subjects/**",
                    "/api/v1/teacher/**",
                    "/api/v1/face/**"
                ).hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")

                // 4. FAQAT ADMIN uchun qolgan barcha admin yo'llari
                auth.requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")

                // 5. Qolgan hamma so'rovlar login talab qiladi
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // AWS'da localhost ishlamaydi, shuning uchun pattern ishlatamiz
        configuration.allowedOriginPatterns = listOf(
            "http://localhost:5173",
            "http://*.timora.uz",
            "https://*.timora.uz",
            "http://api.timora.uz:8081"
        )

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        // Headers bo'limida '*' ishlatish xavfsizroq (Custom headerlar uchun)
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager
}