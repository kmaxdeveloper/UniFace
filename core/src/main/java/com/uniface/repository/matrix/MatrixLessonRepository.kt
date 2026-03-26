package com.uniface.repository.matrix

import com.uniface.entity.matrix.Lesson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixLessonRepository : JpaRepository<Lesson, Long>