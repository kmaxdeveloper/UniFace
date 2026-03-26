package com.uniface.dto.matrix

data class UniversityDataImport(
    val buildings: List<BuildingDTO>,
    val subjects: List<SubjectDTO>,
    val groups: List<GroupDTO>,
    val teachers: List<TeacherDTO>,
    val lessons: List<LessonDTO>
)