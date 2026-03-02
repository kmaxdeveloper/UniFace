package com.uniface.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtils(
    // 1. Secret kalitni konstruktor orqali xavfsiz o'qiymiz
    @Value("\${jwt.secret}") private val secret: String
) {
    private val jwtExpirationMs = 86400000 // 24 soat

    // 2. Secret kalitni SecretKey obyektiga konvertatsiya qilamiz
    // Kod yurganda emas, obyekt yaratilganda initsializatsiya bo'ladi
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}