package com.uniface.matrix.solver

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.Constraint
import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.core.api.score.stream.ConstraintProvider
import ai.timefold.solver.core.api.score.stream.Joiners
import com.uniface.entity.Lesson
import kotlin.math.abs

class MatrixConstraintProvider : ConstraintProvider {

    override fun defineConstraints(factory: ConstraintFactory): Array<Constraint> {
        return arrayOf(
            roomConflict(factory),
            teacherConflict(factory),
            studentGroupConflict(factory),
            capacityConflict(factory),
            laboratoryRequirement(factory),
            facultyBuildingStability(factory),
            minimizeStudentGaps(factory)
        )
    }

    // 1. Xona bandligi (lesson.room va lesson.timeslot ishlatildi)
    private fun roomConflict(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal { it.room },
                Joiners.equal { it.timeslot },
                Joiners.lessThan { it.id ?: 0L }
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room conflict")
    }

    // 2. Ustoz bandligi
    private fun teacherConflict(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal { it.teacher },
                Joiners.equal { it.timeslot },
                Joiners.lessThan { it.id ?: 0L }
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher conflict")
    }

    // 3. Guruhlar to'qnashuvi (lesson.groups ishlatildi)
    private fun studentGroupConflict(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal { it.timeslot },
                Joiners.lessThan { it.id ?: 0L }
            )
            .filter { l1, l2 ->
                l1.groups.any { g1 -> l2.groups.any { g2 -> g1.id == g2.id } }
            }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Student group overlap")
    }

    // 4. Xona sig'imi (Guruhlardagi studentCount yig'indisi)
    private fun capacityConflict(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .filter { lesson ->
                val room = lesson.room ?: return@filter false
                val totalStudents = lesson.groups.sumOf { it.studentCount }
                totalStudents > room.capacity
            }
            .penalize(HardSoftScore.ofHard(10))
            .asConstraint("Capacity exceeded")
    }

    // 5. Laboratoriya talabi (Subject.labHours va Room.isLaboratory)
    private fun laboratoryRequirement(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .filter { lesson ->
                val subject = lesson.subject ?: return@filter false
                val room = lesson.room ?: return@filter false

                // Agar fanda lab soatlari bo'lsa, xona laboratoriya bo'lishi shart
                (subject.labHours > 0) && !room.isLaboratory
            }
            .penalize(HardSoftScore.ofHard(5))
            .asConstraint("Laboratory room requirement")
    }

    // 6. Fakultet binosi (Soft constraint)
    private fun facultyBuildingStability(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .filter { lesson ->
                val firstGroupFaculty = lesson.groups.firstOrNull()?.faculty?.name
                val roomBuildingName = lesson.room?.building?.name

                if (firstGroupFaculty != null && roomBuildingName != null) {
                    !roomBuildingName.contains(firstGroupFaculty, ignoreCase = true)
                } else false
            }
            .penalize(HardSoftScore.ofSoft(10))
            .asConstraint("Faculty building preference")
    }

    // 7. Talabalar uchun "okno"larni (gaps) kamaytirish
    private fun minimizeStudentGaps(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal { it.timeslot?.dayOfWeek }
            )
            .filter { l1, l2 ->
                val commonGroup = l1.groups.any { g1 -> l2.groups.any { g2 -> g1.id == g2.id } }
                val p1 = l1.timeslot?.pairNumber ?: 0
                val p2 = l2.timeslot?.pairNumber ?: 0

                commonGroup && abs(p1 - p2) > 1
            }
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Minimize student gaps")
    }
}