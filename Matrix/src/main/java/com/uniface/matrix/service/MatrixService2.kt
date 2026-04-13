package com.uniface.matrix.service

import ai.timefold.solver.core.api.solver.SolverFactory
import ai.timefold.solver.core.config.solver.SolverConfig
import ai.timefold.solver.core.config.solver.termination.TerminationConfig
import com.uniface.entity.Lesson
import com.uniface.matrix.domain.Timetable
import com.uniface.matrix.solver.MatrixConstraintProvider
import com.uniface.repository.LessonRepository
import com.uniface.repository.matrix.RoomRepository
import com.uniface.repository.matrix.TimeslotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class MatrixService2(
    private val lessonRepository: LessonRepository,
    private val roomRepository: RoomRepository,
    private val timeslotRepository: TimeslotRepository
) {

    @Transactional
    fun generateAndSaveTimetable(): Timetable {
        // 1. Bazadan barcha kerakli ma'lumotlarni yig'amiz
        val lessons = lessonRepository.findAll()
        val rooms = roomRepository.findAll()
        val timeslots = timeslotRepository.findAll()

        // 2. Planning Problem (shartli masala) yaratamiz
        val problem = Timetable(
            timeslots = timeslots,
            rooms = rooms,
            lessons = lessons
        )

        // 3. Masalani yechamiz (AI ishga tushadi)
        val solution = solve(problem)

        // 4. Natijani bazaga saqlaymiz
        // Muhim: Timefold 'lesson' obyektlaridagi 'room' va 'timeslot' fieldlarini to'ldirib beradi
        lessonRepository.saveAll(solution.lessons)

        return solution
    }

    fun solve(problem: Timetable): Timetable {
        val solverConfig = SolverConfig()
            .withSolutionClass(Timetable::class.java)
            .withEntityClasses(Lesson::class.java)
            .withConstraintProviderClass(MatrixConstraintProvider::class.java)
            .withTerminationConfig(TerminationConfig().withSpentLimit(Duration.ofSeconds(30))) // 30 sekund yetarli bo'ladi

        val solverFactory = SolverFactory.create<Timetable>(solverConfig)
        val solver = solverFactory.buildSolver()

        return solver.solve(problem)
    }

    fun printTimetable(solution: Timetable) {
        println("\n--- MATRIX GENERATSIYA NATIJASI ---")
        println("Status: ${solution.score}")

        solution.lessons
            .filter { it.timeslot != null && it.room != null }
            .sortedWith(compareBy({ it.timeslot?.dayOfWeek }, { it.timeslot?.pairNumber }))
            .forEach { lesson ->
                val groupNames = lesson.groups.joinToString(", ") { it.name }
                println("${lesson.timeslot?.dayOfWeek} | ${lesson.timeslot?.pairNumber}-para | " +
                        "Xona: ${lesson.room?.roomNumber} (${lesson.room?.building?.name}) | " +
                        "Fan: ${lesson.subject?.name} | Guruhlar: [$groupNames] | Ustoz: ${lesson.teacher?.fullName}")
            }
    }
}