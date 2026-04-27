package com.uniface.mizan.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono
import com.uniface.mizan.service.GeminiEvaluationResponse
import com.uniface.mizan.service.GeminiService
import com.uniface.mizan.util.DocumentParser

@RestController
@RequestMapping("/api/v1/student/mizan")
@CrossOrigin(origins = ["*"]) // Frontend bilan ishlashi uchun
class MizanController(
    private val documentParser: DocumentParser,
    private val geminiService: GeminiService
) {

    @PostMapping("/evaluate")
    fun evaluateDocument(@RequestParam("file") file: MultipartFile): Mono<ResponseEntity<GeminiEvaluationResponse>> {
        return try {
            val extractedText = documentParser.parseFile(file)
            
            if (extractedText.isBlank()) {
                Mono.just(ResponseEntity.badRequest().build())
            } else {
                geminiService.evaluateDocument(extractedText)
                    .map { response -> ResponseEntity.ok(response) }
            }
        } catch (e: Exception) {
            Mono.just(ResponseEntity.internalServerError().build())
        }
    }
}
