package com.uniface.matrix.solver

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.*
import com.uniface.data.LessonType
import com.uniface.entity.Lesson
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

        // SOFT
        preferEarlyPairs(factory),
        maxLessonsPerDayPerGroup(factory),
        maxLessonsPerDayPerTeacher(factory),
        compactGroupSchedule(factory),
        spreadLessonsEvenly(factory)
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
}