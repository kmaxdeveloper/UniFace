package com.uniface.iris

enum class IrisActivity(val points: Int, val description: String) {
    STUDENT_ATTENDANCE(10, "Darsda qatnashganlik uchun"),
    STUDENT_ASSIGNMENT_ON_TIME(20, "Vazifani vaqtida yuklaganlik uchun"),
    TEACHER_STUDENT_ATTENDANCE(1, "Talabalar davomati uchun (ustozga)"),
    MIZAN_AI_EXCELLENCE(50, "Mizan AI tahlilida yuqori natija uchun")
}
