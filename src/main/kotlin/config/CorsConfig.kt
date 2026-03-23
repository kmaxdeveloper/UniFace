package com.uniface.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Configuration
class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // Eng birinchi shu filtr ishlashi shart!
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        // 1. Credential-larga ruxsat (JWT va Cookie-lar uchun)
        config.allowCredentials = true

        // 2. Qaysi manzillardan so'rov kelsa ruxsat berish
        config.allowedOrigins = listOf(
            "http://localhost:5173",
            "http://16.171.151.104",
            "http://16.171.151.104:8081"
        )

        // 3. Hamma metodlarga ruxsat (OPTIONS, POST, GET va h.k.)
        config.allowedMethods = listOf("*")

        // 4. Hamma headerlarga ruxsat (Authorization, Content-Type va h.k.)
        config.allowedHeaders = listOf("*")

        // 5. Brauzer ko'ra oladigan headerlar
        config.exposedHeaders = listOf("Authorization")

        // 6. Hamma API yo'nalishlariga qo'llash
        source.registerCorsConfiguration("/**", config)

        return CorsFilter(source)
    }
}