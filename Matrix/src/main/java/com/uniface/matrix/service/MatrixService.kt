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
    private val curriculumRepository: CurriculumRepository
) {
    private val log = LoggerFactory.getLogger(MatrixService::class.java)

    // Aktiv job'larni xotirada saqlaymiz
    private val jobs = ConcurrentHashMap<String, JobStatus>()

    // ──────────────────────────────────────────────────────
    // SOLVE BOSHLASH → jobId qaytaradi
    // ──────────────────────────────────────────────────────
    fun startSolving(semester: Int): String {
        val jobId = UUID.randomUUID().toString()

        // 1. Barcha kerakli ma'lumotlarni DB dan olamiz
        val timeSlots = timeSlotRepository.findAllOrdered()
        val rooms     = roomRepository.findAll()
        val lessons   = buildLessons(semester)

        if (lessons.isEmpty())
            throw IllegalStateException("Semester $semester uchun dars topilmadi")

        // 2. Timefold uchun problem tayyorlaymiz
        val problem = TimetableSolution(
            timeSlots = timeSlots,
            rooms     = rooms,
            lessons   = lessons
        )

        // 3. Job statusini SOLVING deb belgilaymiz
        jobs[jobId] = JobStatus(jobId, SolveStatus.SOLVING, semester)

        // 4. Timefold async solve boshlaydi (solveBuilder — 1.6.0+ yangi API)
        solverManager.solveBuilder()
            .withProblemId(jobId)
            .withProblem(problem)
            .withBestSolutionConsumer { solution -> onNewSolution(jobId, solution) }
            .withExceptionHandler { _, error -> onError(jobId, error) }
            .run()

        log.info("✅ Solver started | jobId=$jobId | semester=$semester | lessons=${lessons.size}")
        return jobId
    }

    // ──────────────────────────────────────────────────────
    // STATUS TEKSHIRISH
    // ──────────────────────────────────────────────────────
    fun getStatus(jobId: String): JobStatus =
        jobs[jobId] ?: throw NoSuchElementException("Job topilmadi: $jobId")

    // ──────────────────────────────────────────────────────
    // TO'XTATISH
    // ──────────────────────────────────────────────────────
    fun stopSolving(jobId: String) {
        solverManager.terminateEarly(jobId)
        jobs[jobId]?.let { jobs[jobId] = it.copy(status = SolveStatus.STOPPED) }
        log.info("🛑 Solver stopped | jobId=$jobId")
    }

    // ──────────────────────────────────────────────────────
    // GURUH JADVALI
    // ──────────────────────────────────────────────────────
    fun getGroupTimetable(groupId: Long): List<Lesson> =
        lessonRepository.findByGroupId(groupId)

    // ──────────────────────────────────────────────────────
    // O'QITUVCHI JADVALI
    // ──────────────────────────────────────────────────────
    fun getTeacherTimetable(teacherId: Long): List<Lesson> =
        lessonRepository.findByTeacherId(teacherId)

    // ──────────────────────────────────────────────────────
    // XONA JADVALI
    // ──────────────────────────────────────────────────────
    fun getRoomTimetable(roomId: Long): List<Lesson> =
        lessonRepository.findByRoomId(roomId)

    // ──────────────────────────────────────────────────────
    // JADVAL TOZALASH (qayta solve qilishdan oldin)
    // ──────────────────────────────────────────────────────
    @Transactional
    fun clearTimetable(): Int {
        val scheduled = lessonRepository.findAll().filter { it.timeslot != null }
        scheduled.forEach { it.timeslot = null; it.room = null }
        lessonRepository.saveAll(scheduled)
        log.info("🗑 Timetable cleared | count=${scheduled.size}")
        return scheduled.size
    }

    // ──────────────────────────────────────────────────────
    // PRIVATE: Yangi yaxshi yechim topilganda chaqiriladi
    // ──────────────────────────────────────────────────────
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

            // Faqat hard constraint bajarilgan bo'lsa DB ga saqlaymiz
            if (hard == 0) {
                lessonRepository.saveAll(solution.lessons)
                log.info("💾 Timetable saved | jobId=$jobId")
            }
        } catch (e: Exception) {
            onError(jobId, e)
        }
    }

    // ──────────────────────────────────────────────────────
    // PRIVATE: Xato yuz berganda chaqiriladi
    // ──────────────────────────────────────────────────────
    private fun onError(jobId: String, error: Throwable) {
        log.error("❌ Solver error | jobId=$jobId | ${error.message}", error)
        jobs[jobId]?.let {
            jobs[jobId] = it.copy(
                status  = SolveStatus.FAILED,
                message = error.message
            )
        }
    }

    // ──────────────────────────────────────────────────────
    // PRIVATE: Curriculum + SubjectAllocation → Lesson list
    // ──────────────────────────────────────────────────────
    private fun buildLessons(semester: Int): List<Lesson> {
        val curricula   = curriculumRepository.findBySemester(semester)
        val allocations = subjectAllocationRepository.findAll()
        val lessons     = mutableListOf<Lesson>()

        // --- ID berish uchun counter qo'shildi ---
        var lessonIdCounter = 1L

        log.info("📚 Curriculum count for semester=$semester: ${curricula.size}")
        log.info("📋 Total allocations: ${allocations.size}")

        for (cur in curricula) {
            val subject = cur.subject
            val group   = cur.group

            if (subject == null) { log.warn("⚠️ Curriculum id=${cur.id} — subject NULL, o'tkazib yuborildi"); continue }
            if (group == null)   { log.warn("⚠️ Curriculum id=${cur.id} — group NULL, o'tkazib yuborildi"); continue }

            val allocation = allocations.find {
                it.subject?.id == subject.id && it.group?.id == group.id
            }
            if (allocation == null) {
                log.warn("⚠️ subject=${subject.name}, group=${group.name} uchun SubjectAllocation topilmadi")
                continue
            }

            val teacher = allocation.teacher
            if (teacher == null) {
                log.warn("⚠️ subject=${subject.name}, group=${group.name} — teacher NULL")
                continue
            }

            log.info("✅ ${subject.name} | ${group.name} | ${teacher.fullName} | lecture=${subject.lectureHours} lab=${subject.labHours}")

            repeat(subject.lectureHours) {
                lessons.add(Lesson(
                    subject = subject,
                    teacher = teacher,
                    type    = LessonType.LECTURE
                ).apply {
                    id = lessonIdCounter++ // Solver tanib olishi uchun ID berdik
                    groups.add(group)
                })
            }

            repeat(subject.labHours) {
                lessons.add(Lesson(
                    subject = subject,
                    teacher = teacher,
                    type    = LessonType.LABORATORY
                ).apply {
                    id = lessonIdCounter++ // Solver tanib olishi uchun ID berdik
                    groups.add(group)
                })
            }
        }

        log.info("📋 Built ${lessons.size} lessons for semester=$semester")
        return lessons
    }
}