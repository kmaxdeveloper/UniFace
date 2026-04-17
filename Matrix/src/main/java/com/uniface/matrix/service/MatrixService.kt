package com.uniface.matrix.service

import ai.timefold.solver.core.api.solver.SolverManager
import com.uniface.data.LessonType
import com.uniface.data.SolveStatus
import com.uniface.dto.matrix.JobStatus
import com.uniface.entity.Lesson
import com.uniface.matrix.solver.TimetableSolution
import com.uniface.repository.*
import com.uniface.repository.matrix.RoomRepository
import com.uniface.repository.matrix.TimeslotRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class MatrixService(
    private val solverManager: SolverManager<TimetableSolution, String>,
    private val lessonRepository: LessonRepository,
    private val timeSlotRepository: TimeslotRepository,
    private val roomRepository: RoomRepository,
    private val subjectAllocationRepository: SubjectAllocationRepository,
    private val curriculumRepository: CurriculumRepository,
    private val teacherRepository: TeacherRepository
) {
    private val log = LoggerFactory.getLogger(MatrixService::class.java)

    private val jobs = ConcurrentHashMap<String, JobStatus>()

    fun startSolving(semester: Int): String {
        val jobId = UUID.randomUUID().toString()

        val timeSlots = timeSlotRepository.findAllOrdered()
        val rooms     = roomRepository.findAll()
        val teachers  = teacherRepository.findAll()
        val lessons   = buildLessons(semester)

        if (lessons.isEmpty())
            throw IllegalStateException("Semester $semester uchun dars topilmadi")

        // 🔥 YANGI: DATA CHECK
        val capacity = timeSlots.size * rooms.size
        log.info("📊 STATS | lessons=${lessons.size} | rooms=${rooms.size} | timeslots=${timeSlots.size} | capacity=$capacity")

        if (lessons.size > capacity) {
            log.error("❌ Impossible schedule! lessons > capacity")
            throw IllegalStateException("Impossible schedule: lessons=${lessons.size}, capacity=$capacity")
        }

        val problem = TimetableSolution(
            timeSlots = timeSlots,
            rooms     = rooms,
            lessons   = lessons,
            teachers = teachers
        )

        jobs[jobId] = JobStatus(jobId, SolveStatus.SOLVING, semester)

        solverManager.solveBuilder()
            .withProblemId(jobId)
            .withProblem(problem)
            .withBestSolutionConsumer { solution -> onNewSolution(jobId, solution) }
            .withExceptionHandler { _, error -> onError(jobId, error) }
            .run()

        log.info("✅ Solver started | jobId=$jobId | semester=$semester | lessons=${lessons.size}")
        return jobId
    }

    fun getStatus(jobId: String): JobStatus =
        jobs[jobId] ?: throw NoSuchElementException("Job topilmadi: $jobId")

    fun stopSolving(jobId: String) {
        solverManager.terminateEarly(jobId)
        jobs[jobId]?.let { jobs[jobId] = it.copy(status = SolveStatus.STOPPED) }
        log.info("🛑 Solver stopped | jobId=$jobId")
    }

    fun getGroupTimetable(groupId: Long): List<Lesson> =
        lessonRepository.findByGroupId(groupId)

    fun getTeacherTimetable(teacherId: Long): List<Lesson> =
        lessonRepository.findByTeacherId(teacherId)

    fun getRoomTimetable(roomId: Long): List<Lesson> =
        lessonRepository.findByRoomId(roomId)

    @Transactional
    fun clearTimetable(): Int {
        val scheduled = lessonRepository.findAll()
            .filter { it.timeslot != null || it.room != null }

        scheduled.forEach {
            it.timeslot = null
            it.room = null
        }

        lessonRepository.saveAll(scheduled)
        log.info("🗑 Timetable cleared | count=${scheduled.size}")
        return scheduled.size
    }

    @Transactional
    private fun onNewSolution(jobId: String, solution: TimetableSolution) {
        try {
            val hard = solution.score.hardScore()
            val soft = solution.score.softScore()

            log.info("🔄 New solution | jobId=$jobId | hard=$hard | soft=$soft")

            val newStatus = if (hard == 0) SolveStatus.COMPLETED else SolveStatus.SOLVING
            jobs[jobId]?.let {
                jobs[jobId] = it.copy(
                    status    = newStatus,
                    hardScore = hard,
                    softScore = soft
                )
            }

            // 🔥 DEBUG (xohlasang o‘chirib qo‘yasan)
            if (hard <= 5) {
                lessonRepository.saveAll(solution.lessons)
                log.info("💾 Intermediate save | hard=$hard")
            }

            if (hard == 0) {
                lessonRepository.saveAll(solution.lessons)
                log.info("💾 Final timetable saved | jobId=$jobId")
            }

        } catch (e: Exception) {
            onError(jobId, e)
        }
    }

    private fun onError(jobId: String, error: Throwable) {
        log.error("❌ Solver error | jobId=$jobId | ${error.message}", error)
        jobs[jobId]?.let {
            jobs[jobId] = it.copy(
                status  = SolveStatus.FAILED,
                message = error.message
            )
        }
    }

    private fun buildLessons(semester: Int): List<Lesson> {
        val curricula = curriculumRepository.findBySemester(semester)
        val allocations = subjectAllocationRepository.findAll()
        val lessons = mutableListOf<Lesson>()
        val weeksInSemester = 18 // 💡 TATU standarti bo'yicha 18 hafta

        var lessonIdCounter = 1L
        val allocationMap = allocations.associateBy { it.subject?.id to it.group?.id }

        for (cur in curricula) {
            val subject = cur.subject ?: continue
            val group = cur.group ?: continue
            val allocation = allocationMap[subject.id to group.id] ?: continue
            val teacher = allocation.teacher ?: continue

            // 💡 Haftalik para sonini hisoblaymiz (1 para = 2 soat)
            val weeklyLectures = (subject.lectureHours / 2) / weeksInSemester
            val weeklyLabs = (subject.labHours / 2) / weeksInSemester

            log.info("✅ ${subject.name} | Haftalik reja: Ma'ruza=${weeklyLectures}, Lab=${weeklyLabs}")

            // Ma'ruzalar uchun
            repeat(weeklyLectures) {
                lessons.add(Lesson(
                    subject = subject,
                    teacher = null,
                    type = LessonType.LECTURE
                ).apply {
                    id = lessonIdCounter++
                    groups.add(group)
                })
            }

            // Laboratoriyalar uchun
            repeat(weeklyLabs) {
                lessons.add(Lesson(
                    subject = subject,
                    teacher = null,
                    type = LessonType.LABORATORY
                ).apply {
                    id = lessonIdCounter++
                    groups.add(group)
                })
            }
        }

        log.info("📋 Built ${lessons.size} weekly lessons (paralar) for semester=$semester")
        return lessons
    }
}