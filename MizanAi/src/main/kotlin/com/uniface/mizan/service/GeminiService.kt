package com.uniface.mizan.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class GeminiService(
    private val webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper
) {

    @Value("\${gemini.api.key}")
    private lateinit var apiKey: String

    private val geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"

    fun evaluateDocument(text: String): Mono<GeminiEvaluationResponse> {
        val prompt = """
            Siz universitet talabalarining ishi(referat, taqdimot, kod)ni baholaydigan Mizan AI tizimisiz.
            Quyida talaba tomonidan yuborilgan fayldagi matn berilgan. Matnni o'qib, uni quyidagi mezonlar bo'yicha baholang (0-100%):
            1. Originality (Originallik)
            2. Structural Integrity (Tuzilish va Strukturasi)
            3. Technical Depth (Texnik chuqurlik)
            4. Clarity & Logic (Mantiq va tushunarlilik)
            
            Umumiy ball (score: 0-100) va baho (grade: A, B, C, D, F) chiqaring.
            Shuningdek, o'zbek tilida qisqa va aniq fikr-mulohaza (feedback) yozing.
            
            Iltimos, faqat quyidagi JSON formatida javob bering, boshqa hech qanday so'z qo'shmang (hech qanday markdown emas, toza JSON):
            {
              "score": 85,
              "grade": "B",
              "feedback": "Sizning ishingiz yaxshi...",
              "criteria": [
                { "name": "Originality", "score": 90 },
                { "name": "Structural Integrity", "score": 80 },
                { "name": "Technical Depth", "score": 85 },
                { "name": "Clarity & Logic", "score": 85 }
              ]
            }
            
            Matn:
            $text
        """.trimIndent()

        val requestBody = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to prompt)
                    )
                )
            )
        )

        val webClient = webClientBuilder.build()

        return webClient.post()
            .uri("$geminiApiUrl?key=$apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody ->
                parseGeminiResponse(responseBody)
            }
            .onErrorResume { e ->
                Mono.error(RuntimeException("Gemini API bilan bog'lanishda xatolik: ${e.message}"))
            }
    }

    private fun parseGeminiResponse(responseBody: String): GeminiEvaluationResponse {
        try {
            val rootNode = objectMapper.readTree(responseBody)
            val textContent = rootNode
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText()

            // Gemini ba'zan markdown (```json) qo'shib yuboradi, uni tozalaymiz
            val cleanJson = textContent.replace("```json", "").replace("```", "").trim()
            
            return objectMapper.readValue(cleanJson, GeminiEvaluationResponse::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Gemini javobini o'qishda xatolik: ${e.message}")
        }
    }
}

data class GeminiEvaluationResponse(
    val score: Int,
    val grade: String,
    val feedback: String,
    val criteria: List<EvaluationCriteria>
)

data class EvaluationCriteria(
    val name: String,
    val score: Int
)
