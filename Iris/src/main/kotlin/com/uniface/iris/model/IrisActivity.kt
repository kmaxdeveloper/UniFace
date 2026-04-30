package com.uniface.iris.model

enum class IrisActivity(val points: Double, val description: String) {
    ATTENDANCE_MARK(10.0, "Davomatdan o'tgani uchun"),
    LATE_ATTENDANCE(5.0, "Kechikib bo'lsa ham kelgani uchun"),
    TEACHER_ATTENDANCE_BONUS(2.0, "Har bir kelgan talaba uchun ustozga bonus"),
    ASSIGNMENT_SUBMISSION(50.0, "Topshiriqni vaqtida yuklagani uchun"),
    QUIZ_EXCELLENT(100.0, "Testdan a'lo o'tgani uchun"),
    GAME_REWARD_SMALL(0.1, "O'yin uchun kichik mukofot"),
    GAME_REWARD_BIG(2.0, "O'yinni yirik yutuq bilan yakunlagani uchun")
}
