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

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtAuthFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            // BU YERDA O'ZGARISH: .disable() o'rniga shunchaki .cors { } ✅
            .cors { }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                // OPTIONS so'rovlariga ruxsat berish (ba'zan bu ham kerak bo'ladi)
                auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                auth.requestMatchers("/api/v1/auth/**").permitAll()
                auth.requestMatchers("/api/matrix/admin/**").hasAuthority("ROLE_ADMIN")
                auth.requestMatchers("/api/matrix/public/**").permitAll()

                auth.requestMatchers("/api/v1/admin/groups/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                auth.requestMatchers("/api/v1/admin/subjects/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")

                auth.requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                auth.requestMatchers("/api/v1/teacher/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")
                auth.requestMatchers("/api/v1/face/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER")

                auth.anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}