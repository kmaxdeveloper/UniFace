package com.uniface.data

/**
 * Barcha API javoblari shu format da qaytadi:
 *
 * Muvaffaqiyatli:
 * {
 *   "success": true,
 *   "message": "Jadval tuzildi",
 *   "data": { ... }
 * }
 *
 * Xato:
 * {
 *   "success": false,
 *   "message": "Semester 1 uchun dars topilmadi",
 *   "data": null
 * }
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
) {
    companion object {

        // Muvaffaqiyatli javob — data bilan
        fun <T> success(data: T, message: String = "OK") = ApiResponse(
            success = true,
            message = message,
            data    = data
        )

        // Muvaffaqiyatli javob — data siz (faqat message)
        fun success(message: String) = ApiResponse<Nothing>(
            success = true,
            message = message,
            data    = null
        )

        // Xato javob
        fun <T> error(message: String) = ApiResponse<T>(
            success = false,
            message = message,
            data    = null
        )
    }
}