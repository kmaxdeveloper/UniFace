package com.uniface.matrix.service

import com.uniface.matrix.domain.Timetable
import com.uniface.matrix.solver.MatrixConstraintProvider
import com.uniface.repository.matrix.RoomRepository     // Unifacedan import ✅
import com.uniface.repository.matrix.TimeslotRepository // Unifacedan import ✅
import ai.timefold.solver.core.api.solver.SolverFactory
import ai.timefold.solver.core.config.solver.SolverConfig
import com.uniface.entity.Lesson
import com.uniface.repository.LessonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional // MANA SHU IMPORT SHART ✅
import java.time.Duration

@Service
class MatrixService(
    private val matrixLessonRepository: LessonRepository,
    private val roomRepository: RoomRepository,
    private val timeslotRepository: TimeslotRepository
) {

    @Transactional // Endi xato bermaydi
    fun generateTimetable(): Timetable {
        val problem = Timetable(
            timeslots = timeslotRepository.findAll(),
            rooms = roomRepository.findAll(),
            lessons = matrixLessonRepository.findAll()
        )

        val solution = solve(problem)

        // Hisoblangan natijani bazaga saqlaymiz
        matrixLessonRepository.saveAll(solution.lessons)

        return solution
    }

    fun solve(problem: Timetable): Timetable {
        val solverConfig = SolverConfig()
            .withSolutionClass(Timetable::class.java)
            .withEntityClasses(Lesson::class.java)
            .withConstraintProviderClass(MatrixConstraintProvider::class.java)
            .withTerminationSpentLimit(Duration.ofSeconds(10))

        val solverFactory = SolverFactory.create<Timetable>(solverConfig)
        val solver = solverFactory.buildSolver()

        return solver.solve(problem)
    }

    fun printTimetable(solution: Timetable) {
        println("\n--- GENERATSIYA QILINGAN JADVAL ---")
        solution.lessons.filter { it.timeslot != null && it.room != null }
            .sortedBy { it.timeslot?.id }
            .forEach { lesson ->
                println("${lesson.timeslot?.dayOfWeek} | ${lesson.timeslot?.pairNumber}-para | " +
                        "Bino: ${lesson.room?.building?.name} | Xona: ${lesson.room?.roomNumber} | " +
                        "Fan: ${lesson.subject} | Guruh: ${lesson.groups} | Ustoz: ${lesson.teacher}")
            }
        println("Score: ${solution.score}")
    }
}