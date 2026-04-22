package com.uniface.matrix.solver

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.*
import com.uniface.data.LessonType
import com.uniface.entity.Lesson
import com.uniface.entity.Subject
import com.uniface.entity.Teacher
import kotlin.math.abs

class TimetableConstraintProvider : ConstraintProvider {

    override fun defineConstraints(factory: ConstraintFactory): Array<Constraint> = arrayOf(

        // HARD
        teacherConflict(factory),
        roomConflict(factory),
        groupConflict(factory),
        roomCapacityConflict(factory),
        labRoomRequired(factory),
        lectureNotInLab(factory),
        teacherMustBeQualified(factory),
        teacherMaxLessonsPerDay(factory),
        minimizeGroupGaps(factory),
        minimizeTeacherGaps(factory),

        // SOFT
        preferEarlyPairs(factory),
        maxLessonsPerDayPerGroup(factory),
        maxLessonsPerDayPerTeacher(factory),
        compactGroupSchedule(factory),
        spreadLessonsEvenly(factory),
        compactTeacherSchedule(factory),
        spreadTeacherLessonsEvenly(factory),
        avoidTooManyConsecutiveLessons(factory),
        groupRoomStability(factory),
        distributeTeacherLoadEvenly(factory)
    )

    // ───────────────── HARD ─────────────────

    private fun teacherConflict(factory: ConstraintFactory): Constraint =
        factory.forEachUniquePair(
            Lesson::class.java,
            Joiners.equal { it.timeslot?.id },
            Joiners.equal { it.teacher?.id }
        )
            .filter { a, _ -> a.teacher != null && a.timeslot != null }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher conflict")

    private fun roomConflict(factory: ConstraintFactory): Constraint =
        factory.forEachUniquePair(
            Lesson::class.java,
            Joiners.equal { it.timeslot?.id },
            Joiners.equal { it.room?.id }
        )
            .filter { a, _ -> a.room != null && a.timeslot != null }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room conflict")

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

    private fun roomCapacityConflict(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.room != null }
            .penalize(HardSoftScore.ONE_HARD) { lesson ->
                val students = lesson.groups.sumOf { it.studentCount }
                val capacity = lesson.room!!.capacity
                if (capacity >= students) 0 else students - capacity
            }
            .asConstraint("Room capacity exceeded")

    private fun labRoomRequired(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.type == LessonType.LABORATORY && it.room?.isLaboratory == false }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Lab needs laboratory room")

    private fun lectureNotInLab(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.type == LessonType.LECTURE && it.room?.isLaboratory == true }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Lecture not in lab room")

    private fun minimizeGroupGaps(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null && it.groups.isNotEmpty() }
            .groupBy(
                { it.groups.first().id },
                { it.timeslot!!.dayOfWeek },
                ConstraintCollectors.count()
            )
            .filter { _, _, count -> count > 1 }
            .penalize(HardSoftScore.ONE_HARD) { _, _, count -> count - 1 }
            .asConstraint("Minimize gaps in group schedule")

    private fun minimizeTeacherGaps(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null && it.teacher != null }
            .groupBy(
                { it.teacher!!.id },
                { it.timeslot!!.dayOfWeek },
                ConstraintCollectors.count()
            )
            .filter { _, _, count -> count > 1 }
            .penalize(HardSoftScore.ONE_HARD) { _, _, count -> count - 1 }
            .asConstraint("Minimize gaps in teacher schedule")

    // ───────────────── SOFT ─────────────────

    private fun preferEarlyPairs(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null && it.timeslot!!.pairNumber > 4 }
            .penalize(HardSoftScore.ONE_SOFT) { lesson ->
                (lesson.timeslot!!.pairNumber - 4) * 2
            }
            .asConstraint("Prefer early pairs")

    private fun maxLessonsPerDayPerGroup(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null && it.groups.isNotEmpty() }
            .groupBy(
                { it.timeslot!!.dayOfWeek },
                { it.groups.first().id },
                ConstraintCollectors.count()
            )
            .filter { _, _, count -> count > 3 }
            .penalize(HardSoftScore.ONE_SOFT) { _, _, count -> count - 3 }
            .asConstraint("Max 3 lessons per day per group")

    private fun maxLessonsPerDayPerTeacher(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null && it.teacher != null }
            .groupBy(
                { it.timeslot!!.dayOfWeek },
                { it.teacher!!.id },
                ConstraintCollectors.count()
            )
            .filter { _, _, count -> count > 4 }
            .penalize(HardSoftScore.ONE_SOFT) { _, _, count -> count - 4 }
            .asConstraint("Max 4 lessons per day per teacher")

    private fun spreadLessonsEvenly(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null }
            .groupBy(
                { it.groups.firstOrNull()?.id },
                { it.timeslot!!.dayOfWeek },
                ConstraintCollectors.count()
            )
            .filter { groupId, _, count -> groupId != null && count > 2 }
            .penalize(HardSoftScore.ofSoft(3)) { _, _, count ->
                (count - 2) * 2
            }
            .asConstraint("Spread lessons evenly")

    private fun compactGroupSchedule(factory: ConstraintFactory): Constraint =
        factory.forEachUniquePair(
            Lesson::class.java,
            Joiners.equal { it.groups.firstOrNull()?.id },
            Joiners.equal { it.timeslot?.dayOfWeek }
        )
            .filter { a, b ->
                a.timeslot != null && b.timeslot != null &&
                        abs(a.timeslot!!.pairNumber - b.timeslot!!.pairNumber) > 1
            }
            .penalize(HardSoftScore.ONE_SOFT) { a, b ->
                abs(a.timeslot!!.pairNumber - b.timeslot!!.pairNumber) - 1
            }
            .asConstraint("Compact group schedule")

    // Buni "HARD" bo'limiga qo'sh:
    // 1. HARD CONSTRAINT: O'qituvchi faqat o'zi bilsan fanni o'tsin
    private fun teacherMustBeQualified(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .filter { lesson ->
                val teacher = lesson.teacher
                val subject = lesson.subject

                // 🔥 MUHIM: Ham teacher, ham subject null emasligini tekshiramiz
                // Shunda Kotlin 'subject'ni Subject? dan Subject ga avtomat o'giradi (Smart Cast)
                teacher != null && subject != null && !isQualified(teacher, subject)
            }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher not qualified for subject")
    }

    // 2. Yordamchi mantiq (Logikani shu yerda kengaytiramiz)
    private fun isQualified(teacher: Teacher, subject: Subject): Boolean {
        // 1-darajali tekshiruv: Ustozning subjects ro'yxatida shu fan bormi?
        val hasSubjectSkill = teacher.subjects.any { it.id == subject.id }

        // Kelajakda bu yerga qo'shimcha shartlar qo'shish mumkin:
        // Masalan: Faqat "Professor"lar ma'ruza o'tsin, "Assistent"lar faqat lab o'tsin
        // if (lessonType == LECTURE && teacher.rank != PROFESSOR) return false

        return hasSubjectSkill
    }

    // Buni "SOFT" bo'limiga qo'sh:
    private fun compactTeacherSchedule(factory: ConstraintFactory): Constraint =
        factory.forEachUniquePair(
            Lesson::class.java,
            Joiners.equal { it.teacher?.id },
            Joiners.equal { it.timeslot?.dayOfWeek }
        )
            .filter { a, b ->
                a.timeslot != null && b.timeslot != null &&
                        abs(a.timeslot!!.pairNumber - b.timeslot!!.pairNumber) > 1
            }
            .penalize(HardSoftScore.ONE_SOFT) { a, b ->
                abs(a.timeslot!!.pairNumber - b.timeslot!!.pairNumber) - 1
            }
            .asConstraint("Compact teacher schedule")

    private fun spreadTeacherLessonsEvenly(factory: ConstraintFactory): Constraint =
        factory.forEach(Lesson::class.java)
            .filter { it.timeslot != null && it.teacher != null }
            .groupBy(
                { it.teacher!!.id },
                { it.timeslot!!.dayOfWeek },
                ConstraintCollectors.count()
            )
            .filter { _, _, count -> count > 3 } // Kuniga 3 tadan ko'p dars bo'lsa jarima
            .penalize(HardSoftScore.ofSoft(2)) { _, _, count -> (count - 3) * 2 }
            .asConstraint("Spread teacher lessons evenly")

    /**
     * Domlalar ketma-ket 3-4 ta para dars o'tsa, charchab qolishadi.
     * AIga "ketma-ket darslarni kamaytir" deymiz
     */
    private fun avoidTooManyConsecutiveLessons(factory: ConstraintFactory): Constraint {
        return factory.forEachUniquePair(Lesson::class.java,
            Joiners.equal { it.teacher?.id },
            Joiners.equal { it.timeslot?.dayOfWeek }
        )
            .filter { a, b ->
                // Agar darslar ketma-ket bo'lsa (masalan 1 va 2, yoki 2 va 3)
                abs(a.timeslot!!.pairNumber - b.timeslot!!.pairNumber) == 1
            }
            // Bu darslar soni ko'paygani sari jarima ham o'sib boradi
            .penalize(HardSoftScore.ofSoft(5))
            .asConstraint("Avoid too many consecutive lessons")
    }

    /**
     * Talabalar har para har xil korpus yoki qavatga yugurib yurmasligi uchun,
     * bir guruhning darslarini imkon qadar bitta xonada (agar xona turi to'g'ri kelsa)
     * saqlashga harakat qilamiz
     */
    private fun groupRoomStability(factory: ConstraintFactory): Constraint {
        return factory.forEachUniquePair(Lesson::class.java,
            Joiners.equal { it.groups.firstOrNull()?.id },
            Joiners.equal { it.timeslot?.dayOfWeek },
            Joiners.equal { it.room?.id }
        )
            // Agar bir xil xonada qolishsa, ularni mukofotlaymiz
            .reward(HardSoftScore.ofSoft(2))
            .asConstraint("Group room stability")
    }

    // MatrixConstraintProvider.kt ichiga

    // MatrixConstraintProvider.kt

    fun teacherMaxLessonsPerDay(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEach(Lesson::class.java)
            .filter { it.timeslot != null && it.teacher != null }
            .groupBy(
                { it.teacher!! },
                { it.timeslot!!.dayOfWeek },
                ConstraintCollectors.count()
            )
            .filter { _, _, count -> count > 4 }
            // Yangi API: penalize ichida Score ob'ekti berilmaydi, faqat int jarima beriladi
            .penalize(HardSoftScore.ONE_HARD) { _, _, count -> (count - 4) * 10 }
            .asConstraint("Teacher max 4 lessons per day")
    }

    fun distributeTeacherLoadEvenly(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEach(Lesson::class.java)
            .filter { it.teacher != null }
            .groupBy(
                { it.teacher!! },
                ConstraintCollectors.count()
            )
            // Faqat jarima hisoblanadi (int)
            .penalize(HardSoftScore.ONE_SOFT) { _, count -> count * count }
            .asConstraint("Distribute teacher load evenly")
    }
}