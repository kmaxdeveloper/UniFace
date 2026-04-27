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

    fun evaluateDocument(text: String, subjectName: String? = null): Mono<GeminiEvaluationResponse> {
        val subjectContext = if (!subjectName.isNullOrBlank()) "Sen aynan '$subjectName' fani bo'yicha qattiqqo'l va adolatli universitet o'qituvchisisan." else "Sen universitet o'qituvchisisan."
        
        val prompt = """
            $subjectContext
            Quyida senga talaba tomonidan yozilgan topshiriq (referat, maqola yoki kod) taqdim etiladi.
            Vazifang - bu ishni diqqat bilan o'qib, maksimal 100 ballik tizimda adolatli baholash.
            
            Shuningdek, quyidagilarga qat'iy e'tibor qarat:
            1. Ishning AI (Sun'iy intellekt) tomonidan yozilganlik ehtimolini aniqla.
            2. Plagiat (ko'chirmachilik) bor-yo'qligini tekshir.
            3. Aynan $subjectName fani doirasida yozilganmi yoki mavzudan chetlashganmi, shuni bahola.

            Quyidagi 4 ta mezon bo'yicha baho ber (har biri 0-100%):
            1. Originality (Originallik, AI yoki plagiat emasligi)
            2. Structural Integrity (Tuzilish va Strukturasi)
            3. Technical Depth (Texnik chuqurlik, fan doirasidagi bilim)
            4. Clarity & Logic (Mantiq va tushunarlilik)
            
            Umumiy ball (score: 0-100) va harfiy baho (grade: A, B, C, D, F) chiqaring.
            Shuningdek, o'zbek tilida aniq, asosli va foydali fikr-mulohaza (feedback) yozing. Feedback da albatta AI ulushi va plagiat bor-yo'qligiga to'xtalib o'ting.
            
            Iltimos, faqat quyidagi JSON formatida javob bering, boshqa hech qanday izoh yoki belgilar (```json kabi) qo'shmang:
            {
              "score": 85,
              "grade": "B",
              "feedback": "Ishingiz yaxshi yozilgan, ammo xulosa qismida AI yordamidan foydalanilganlik belgilari bor...",
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
