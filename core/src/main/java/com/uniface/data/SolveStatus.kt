package com.uniface.data

enum class SolveStatus {
    SOLVING,    // Hali ishlayapti
    COMPLETED,  // Muvaffaqiyatli tugadi (hard=0)
    STOPPED,    // Qo'lda to'xtatildi
    FAILED      // Xato yuz berdi
}