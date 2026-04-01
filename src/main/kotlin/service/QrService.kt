package com.uniface.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*

@Service
class QrService {
    private val secretString = "Uniface_TATU_Neura_QR_Secure_2026_High_Level_Protection_Key"
    private val secretKey = Keys.hmacShaKeyFor(secretString.toByteArray())

    fun generateQrToken(lessonId: Long): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .setSubject(lessonId.toString())
            .setIssuedAt(Date(now))
            // 15 soniya - skrinshot qilib tarqatishga ulgurmaydi!
            .setExpiration(Date(now + 30000))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getLessonIdFromToken(token: String): Long? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                // Clock skewni 0 qilamiz, vaqt bo'yicha ayovsiz tekshiruv
                .setAllowedClockSkewSeconds(0)
                .build()
                .parseClaimsJws(token)

            claims.body.subject.toLong()
        } catch (e: Exception) {
            // Token muddati o'tgan bo'lsa darrov null qaytadi
            null
        }
    }
}