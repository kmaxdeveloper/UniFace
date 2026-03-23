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

        config.allowCredentials = true
        config.allowedOrigins = listOf("http://localhost:5173") // React porti
        config.allowedHeaders = listOf("Origin", "Content-Type", "Accept", "Authorization")
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")

        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}