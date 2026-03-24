package com.uniface.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*

@Service
class QrService {
    // 32 belgidan kam bo'lmagan maxfiy kalit
    private val secretKey = Keys.hmacShaKeyFor("Uniface_TATU_Neura_QR_Attendance_Key_2026".toByteArray())

    // O'qituvchi uchun token generatsiya qilish
    fun generateQrToken(lessonId: Long): String {
        return Jwts.builder()
            .setSubject(lessonId.toString())
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 10000)) // 10 soniya (8s + 2s tarmoq uchun)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    // Talaba yuborgan tokendan lessonId ni sug'urib olish
    fun getLessonIdFromToken(token: String): Long? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)

            claims.body.subject.toLong()
        } catch (e: Exception) {
            null // Muddati o'tgan yoki soxta token
        }
    }
}