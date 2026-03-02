package com.uniface.config

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

@Configuration
@EnableWebSecurity
class SecurityConfig {

    // 1. PasswordEncoder - parollarni ochiq holda emas, BCrypt bilan shifrlab saqlash uchun
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // 2. HttpSecurity va SecurityFilterChain - kirish huquqlarini boshqarish
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // REST API bo'lgani uchun CSRF'ni o'chiramiz
            .cors { it.disable() } // Brauzer bilan konflikt bo'lmasligi uchun
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Token bilan ishlash uchun
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/auth/**").permitAll()    // Login hamma uchun ochiq
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // Faqat admin uchun
                    .requestMatchers("/api/v1/face/**").hasAnyRole("ADMIN", "TEACHER") // Davomat ikkalasi uchun
                    .anyRequest().authenticated() // Qolgan hamma narsaga ruxsat kerak
            }
            // Bu yerga hali JwtFilter qo'shamiz, hozircha shunday tursin
            .httpBasic { } // Test qilish uchun vaqtincha httpBasic qo'shib tursa bo'ladi

        return http.build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}