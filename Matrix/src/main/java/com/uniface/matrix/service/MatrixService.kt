package com.uniface.matrix.service

import com.uniface.matrix.engine.MatrixEngine
import com.uniface.repository.*
import com.uniface.repository.matrix.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MatrixService(
    private val allocRepo: SubjectAllocationRepository,
    private val roomRepo: RoomRepository,
    private val slotRepo: TimeslotRepository,
    private val lessonRepo: LessonRepository, // Bu sening LessonRepo'ng
    private val engine: MatrixEngine
) {

    @Transactional
    fun startSolver(): Int {
        lessonRepo.deleteAll() // Eskisini tozalaymiz

        val lessons = engine.generate(
            allocRepo.findAll(),
            roomRepo.findAll(),
            slotRepo.findAll()
        )

        lessonRepo.saveAll(lessons)
        return lessons.size
    }
}