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
    @Value("\${jwt.secret}") private val secret: String
) {
    // 24 soatni o'zgaruvchiga olganing yaxshi, lekin uni ham @Value qilsa bo'ladi
    private val jwtExpirationMs = 86400000

    // Obyekt yaratilgan zahoti byte arrayga o'girib kalitni tayyorlaymiz
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    fun generateToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            // SignatureAlgorithm.HS256 o'rniga shunchaki keyni o'zini bersang ham bo'ladi
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    // Tokenni validate qilishda xatoliklarni logga chiqarish foydali
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            // Jigar, bu yerda e.message ni log qilsang, token nega o'tmaganini bilasan (muddati o'tganmi yoki buzilganmi)
            false
        }
    }
}