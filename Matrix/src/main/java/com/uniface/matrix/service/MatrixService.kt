package com.uniface.matrix.service

import ai.timefold.solver.core.api.solver.SolverFactory
import ai.timefold.solver.core.config.solver.SolverConfig
import ai.timefold.solver.core.config.solver.termination.TerminationConfig
import com.uniface.data.LessonType
import com.uniface.entity.Lesson
import com.uniface.matrix.domain.Timetable
import com.uniface.matrix.engine.MatrixEngine
import com.uniface.matrix.solver.MatrixConstraintProvider
import com.uniface.repository.*
import com.uniface.repository.matrix.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class MatrixService(
    private val lessonRepository: LessonRepository,
    private val allocationRepository: SubjectAllocationRepository, // Admin kiritgan reja
    private val roomRepository: RoomRepository,
    private val timeslotRepository: TimeslotRepository,
    private val teacherRepository: TeacherRepository,
    private val groupRepository: StudentGroupRepository
) {

    @Transactional
    fun runFullAutomation(): Timetable {
        // 1. Eski darslarni tozalaymiz (Yangi jadval uchun joy ochamiz)
        lessonRepository.deleteAll()

        // 2. Bazadan barcha resurslarni va rejalarni olamiz
        val allocations = allocationRepository.findAll()
        val rooms = roomRepository.findAll()
        val timeslots = timeslotRepository.findAll()
        val teachers = teacherRepository.findAll()
        val groups = groupRepository.findAll()

        // 3. SubjectAllocation'dan xomaki Lesson'larni yasaymiz
        val lessonsToSolve = mutableListOf<Lesson>()
        allocations.forEach { allocation ->
            // Masalan: Agar haftasiga 2 marta dars bo'lishi kerak bo'lsa
            // (Hozircha sodda qilib 1 ta allocationdan 1 ta dars yasaymiz)
            val lesson = Lesson(
                subject = allocation.subject,
                teacher = allocation.teacher,
                type = LessonType.PRACTICE, // yoki allocation'dan keladi
                isActive = true
            )
            // Agar bu patok bo'lsa yoki guruh biriktirilgan bo'lsa
            if (allocation.group != null) {
                lesson.groups.add(allocation.group!!)
            }
            lessonsToSolve.add(lesson)
        }

        // 4. Problem yaratamiz
        val problem = Timetable(
            timeslots = timeslots,
            rooms = rooms,
            teachers = teachers,
            studentGroups = groups,
            lessons = lessonsToSolve
        )

        // 5. Solverni yurgizamiz
        val solverConfig = SolverConfig()
            .withSolutionClass(Timetable::class.java)
            .withEntityClasses(Lesson::class.java)
            .withConstraintProviderClass(MatrixConstraintProvider::class.java)
            .withTerminationConfig(TerminationConfig().withSpentLimit(Duration.ofSeconds(60))) // Kattaroq baza uchun 1 min

        val solverFactory = SolverFactory.create<Timetable>(solverConfig)
        val solver = solverFactory.buildSolver()
        val solution = solver.solve(problem)

        // 6. Natijani bazaga saqlaymiz
        lessonRepository.saveAll(solution.lessons)

        return solution
    }
}