package com.uniface.matrix.solver

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.*
import com.uniface.data.LessonType
import com.uniface.entity.Lesson
import kotlin.math.abs

/**
 * Barcha qoidalar shu yerda.
 *
 * HARD constraint → buzilsa jadval YAROQSIZ
 * SOFT constraint → buzilsa score PASAYADI (lekin jadval yaroqli)
 *
 * Timefold score'ni maksimumga chiqarishga harakat qiladi:
 *   Maqsad: hardScore = 0, softScore = imkon qadar katta
 */
class TimetableConstraintProvider : ConstraintProvider {

    override fun defineConstraints(factory: ConstraintFactory): Array<Constraint> = arrayOf(

        // ── HARD ──────────────────────────────────────────
        teacherConflict(factory),
        roomConflict(factory),
        groupConflict(factory),
        roomCapacityConflict(factory),
        labRoomRequired(factory),
        lectureNotInLab(factory),

        // ── SOFT ──────────────────────────────────────────
        preferEarlyPairs(factory),
        maxLessonsPerDayPerGroup(factory),
        maxLessonsPerDayPerTeacher(factory),
        // Buni defineConstraints massiviga qo'shib qo'y
        compactGroupSchedule(factory),
        spreadLessonsEvenly(factory)
    )

    // ──────────────────────────────────────────────────────
    // HARD 1: Bir o'qituvchi bir vaqtda 2 ta dars bera olmaydi
    // ──────────────────────────────────────────────────────
    private fun teacherConflict(factory: ConstraintFactory): Constraint =
        factory.forEachUniquePair(
            Lesson::class.java,
            Joiners.equal { it.timeslot?.id },
            Joiners.equal { it.teacher?.id }
        )
            .filter { a, _ -> a.teacher != null && a.timeslot != null }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher conflict")

    // ──────────────────────────────────────────────────────
    // HARD 2: Bir xona bir vaqtda 2 ta darsga band bo'lmaydi
    // ──────────────────────────────────────────────────────
    private fun roomConflict(factory: ConstraintFactory): Constraint =
        factory.forEachUniquePair(
            Lesson::class.java,
            Joiners.equal { it.timeslot?.id },
            Joiners.equal { it.room?.id }
        )
            .filter { a, _ -> a.room != null && a.timeslot != null }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room conflict")

    // ──────────────────────────────────────────────────────
    // HARD 3: Bir guruh bir vaqtda 2 ta darsda bo'lmaydi
    // ──────────────────────────────────────────────────────
    private fun groupConflict(factory: ConstraintFactory): Constraint =
        factory.forEachUniquePair(
            Lesson::class.java,
            Joiners.equal { it.timeslot?.id }
        )
            .filter { a, b ->
                a.timeslot != null &&
                        a.groups.any { ga -> b.groups.any { gb -> ga.id == gb.id } }
            }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Group conflict")

    // ──────────────────────────────────────────────────────
    // HARD 4: Xona sig'imi talabalar sonidan kam bo'lmaydi
    // ──────────────────────────────────────────────────────
    private fun roomCapacityConflict(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { lesson ->
                val students = lesson.groups.sumOf { it.studentCount }
                val capacity = lesson.room?.capacity ?: 0
                lesson.room != null && capacity < students
            }
            .penalize(HardSoftScore.ONE_HARD) { lesson ->
                val students = lesson.groups.sumOf { it.studentCount }
                val capacity = lesson.room?.capacity ?: 0
                students - capacity   // qancha oshsa, shuncha penalize
            }
            .asConstraint("Room capacity exceeded")

    // ──────────────────────────────────────────────────────
    // HARD 5: LAB darsi faqat laboratoriyada o'tiladi
    // ──────────────────────────────────────────────────────
    private fun labRoomRequired(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.type == LessonType.LABORATORY && it.room?.isLaboratory == false }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Lab needs laboratory room")

    // ──────────────────────────────────────────────────────
    // HARD 6: LECTURE laboratoriyada o'tilmaydi
    // ──────────────────────────────────────────────────────
    private fun lectureNotInLab(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.type == LessonType.LECTURE && it.room?.isLaboratory == true }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Lecture not in lab room")

    // ──────────────────────────────────────────────────────
    // SOFT 1: Kechki paralardan qoching (5-6 para yomon)
    // ──────────────────────────────────────────────────────
    private fun preferEarlyPairs(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { (it.timeslot?.pairNumber ?: 0) > 4 }
            .penalize(HardSoftScore.ONE_SOFT) { lesson ->
                (lesson.timeslot?.pairNumber ?: 0) - 4
            }
            .asConstraint("Prefer early pairs")

    // ──────────────────────────────────────────────────────
    // SOFT 2: Guruh uchun bir kunda 3 dan ko'p dars bo'lmasin
    // ──────────────────────────────────────────────────────
    private fun maxLessonsPerDayPerGroup(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null }
            .groupBy(
                { it.timeslot!!.dayOfWeek },
                { it.groups.firstOrNull()?.id },
                ConstraintCollectors.count()
            )
            .filter { _, _, count -> count > 3 }
            .penalize(HardSoftScore.ONE_SOFT) { _, _, count -> count - 3 }
            .asConstraint("Max 3 lessons per day per group")

    // ──────────────────────────────────────────────────────
    // SOFT 3: O'qituvchi uchun bir kunda 4 dan ko'p dars bo'lmasin
    // ──────────────────────────────────────────────────────
    private fun maxLessonsPerDayPerTeacher(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null && it.teacher != null }
            .groupBy(
                { it.timeslot!!.dayOfWeek },
                { it.teacher?.id },
                ConstraintCollectors.count()
            )
            .filter { _, _, count -> count > 4 }
            .penalize(HardSoftScore.ONE_SOFT) { _, _, count -> count - 4 }
            .asConstraint("Max 4 lessons per day per teacher")

    // ──────────────────────────────────────────────────────
// SOFT: Darslarni haftaga tekis yoyish (Majburlash)
// ──────────────────────────────────────────────────────
    private fun spreadLessonsEvenly(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .groupBy(
                { it.groups.firstOrNull()?.id },
                { it.timeslot?.dayOfWeek },
                ConstraintCollectors.count()
            )
            // Agar bir kunda darslar soni 2 tadan oshsa, ko'proq jazo beramiz
            // Bu solverni dars yo'q kunlarga dars qidirishga majbur qiladi
            .filter { _, _, count -> count > 2 }
            .penalize(HardSoftScore.ofSoft(10)) { _, _, count -> count - 2 }
            .asConstraint("Spread lessons evenly")

    // ──────────────────────────────────────────────────────
// SOFT: Guruh darslari orasida oyna (bo'shliq) bo'lmasin
// ──────────────────────────────────────────────────────
    private fun compactGroupSchedule(factory: ConstraintFactory): Constraint =
        factory.forEachUniquePair(
            Lesson::class.java,
            Joiners.equal { it.groups.firstOrNull()?.id },
            Joiners.equal { it.timeslot?.dayOfWeek }
        )
            .filter { a, b ->
                val diff = abs((a.timeslot?.pairNumber ?: 0) - (b.timeslot?.pairNumber ?: 0))
                diff > 1 // Agar darslar orasida 1 tadan ko'p para bo'sh bo'lsa
            }
            .penalize(HardSoftScore.ONE_SOFT) { a, b ->
                abs((a.timeslot?.pairNumber ?: 0) - (b.timeslot?.pairNumber ?: 0))
            }
            .asConstraint("Compact group schedule")
}