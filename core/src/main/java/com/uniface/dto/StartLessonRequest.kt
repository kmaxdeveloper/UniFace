package com.uniface.dto

import com.uniface.data.LessonType

data class StartLessonRequest(
    val subjectId: Long,
    val teacherUsername: String? = null,
    // Bitta yoki bir nechta guruhni yubora olish uchun List qilamiz
    val groupIds: List<Long>,
    // Dars turi: LECTURE (Ma'ruza) yoki PRACTICE (Amaliyot)
    val lessonType: LessonType = LessonType.PRACTICE,
    // Yangi: Sillabusdagi mavzu ID-si
    val topicId: Long? = null
)