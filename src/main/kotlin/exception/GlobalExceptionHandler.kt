package com.uniface.exception

import com.uniface.data.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.*

@RestControllerAdvice
class GlobalExceptionHandler {

    // 1. 404 - Topilmagan ma'lumotlar (Student, Subject, Lesson)
    @ExceptionHandler(value = [StudentNotFoundException::class, NoSuchElementException::class])
    fun handleNotFound(e: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            message = e.message ?: "So'ralgan ma'lumot topilmadi!",
            status = HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    // 2. 409 - Ziddiyat (Allaqachon davomatdan o'tgan bo'lsa)
    @ExceptionHandler(AlreadyMarkedException::class)
    fun handleConflict(e: AlreadyMarkedException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            message = e.message ?: "Siz allaqachon davomatdan o'tgansiz!",
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    // 3. 422 - Mantiqiy xato (QR eskirgan, vaqt xatosi)
    @ExceptionHandler(InvalidAttendanceException::class)
    fun handleUnprocessable(e: InvalidAttendanceException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            message = e.message ?: "Davomatni amalga oshirib bo'lmadi (mantiqiy xato)",
            status = HttpStatus.UNPROCESSABLE_ENTITY.value()
        )
        return ResponseEntity(error, HttpStatus.UNPROCESSABLE_ENTITY)
    }

    // 4. 400 - Umumiy biznes xatolar
    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(e: BusinessException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            message = e.message ?: "Biznes mantiqda xatolik yuz berdi",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    // 5. 500 - Kutilmagan texnik xatolar (Hamma tushib qoladigan oxirgi "setka")
    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<ErrorResponse> {
        // Console'da biz ko'rishimiz uchun xatoni to'liq chiqaramiz
        e.printStackTrace()

        val error = ErrorResponse(
            message = "Tizimda kutilmagan texnik nosozlik yuz berdi",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}