package com.uniface.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*

@Service
class QrService {
    // Kalitni string ko'rinishida saqlash ishonchliroq
    private val secretString = "Uniface_TATU_Neura_QR_Attendance_Key_2026_Secure_Long_Key"
    private val secretKey = Keys.hmacShaKeyFor(secretString.toByteArray())

    fun generateQrToken(lessonId: Long): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .setSubject(lessonId.toString())
            .setIssuedAt(Date(now))
            // Muddatni 30 soniya qilamiz - tarmoq kechikishlari va skanerlash vaqti uchun
            .setExpiration(Date(now + 30000))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getLessonIdFromToken(token: String): Long? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)

            // Qo'shimcha tekshiruv: Token muddati o'tgan bo'lsa parser o'zi Exception otadi
            claims.body.subject.toLong()
        } catch (e: Exception) {
            // Log yozish foydali: masalan ExpiredJwtException yoki SignatureException
            println("QR Token Error: ${e.message}")
            null
        }
    }
}