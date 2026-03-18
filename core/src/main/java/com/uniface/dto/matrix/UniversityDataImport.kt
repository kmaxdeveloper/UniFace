package com.uniface.dto.matrix

data class UniversityDataImport(
    val buildings: List<com.uniface.dto.matrix.BuildingDTO>,
    val subjects: List<com.uniface.dto.matrix.SubjectDTO>,
    val groups: List<com.uniface.dto.matrix.GroupDTO>,
    val teachers: List<com.uniface.dto.matrix.TeacherDTO>,
    val lessons: List<com.uniface.dto.matrix.LessonDTO>
)