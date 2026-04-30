package com.uniface.iris.model

enum class IrisActivity(val points: Int, val description: String) {
    ATTENDANCE_MARK(10, "Davomatdan o'tgani uchun"),
    LATE_ATTENDANCE(5, "Kechikib bo'lsa ham kelgani uchun"),
    TEACHER_ATTENDANCE_BONUS(2, "Har bir kelgan talaba uchun ustozga bonus"),
    ASSIGNMENT_SUBMISSION(50, "Topshiriqni vaqtida yuklagani uchun"),
    QUIZ_EXCELLENT(100, "Testdan a'lo o'tgani uchun"),
    GAME_REWARD(2, "Mini-o'yin o'ynagani uchun")
}
