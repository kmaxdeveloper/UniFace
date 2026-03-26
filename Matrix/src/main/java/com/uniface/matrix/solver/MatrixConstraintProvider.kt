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
            facultyBuildingStability(factory),
            minimizeStudentGaps(factory)
        )
    }

    private fun roomConflict(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal { lesson -> lesson.room },
                Joiners.equal { lesson -> lesson.timeslot },
                Joiners.lessThan { lesson -> lesson.id ?: 0L } // ID null bo'lsa 0 deb oladi
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room conflict")
    }

    private fun teacherConflict(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal { lesson -> lesson.teacher },
                Joiners.equal { lesson -> lesson.timeslot },
                Joiners.lessThan { lesson -> lesson.id ?: 0L } // Bu yerda ham nullable ID ni hal qilamiz
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher conflict")
    }

    private fun studentGroupConflict(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal { lesson -> lesson.group },    // Lesson::group o'rniga
                Joiners.equal { lesson -> lesson.timeslot }, // Lesson::timeslot o'rniga
                Joiners.lessThan { lesson -> lesson.id ?: 0L } // ID null bo'lsa 0 deb oladi
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Student group conflict")
    }

    // 4. Talabalar soni xona sig'imidan oshib ketmasin
    private fun capacityConflict(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .filter { lesson ->
                val sCount = lesson.group?.studentCount ?: 0
                val rCap = lesson.room?.capacity ?: 0
                // Xona borligini va sig'im yetarli emasligini tekshiramiz
                lesson.room != null && sCount > rCap
            }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Capacity conflict")
    }

    // 5. Fakultet darslari o'z binosida bo'lsin
    private fun facultyBuildingStability(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .filter { lesson ->
                val fName = lesson.group?.faculty?.name
                // BU YERDA: .name qo'shildi, chunki building bu obyekt ✅
                val bName = lesson.room?.building?.name

                if (fName != null && bName != null) {
                    !bName.contains(fName, ignoreCase = true)
                } else {
                    false
                }
            }
            .penalize(HardSoftScore.ofSoft(10))
            .asConstraint("Faculty building preference")
    }

    private fun minimizeStudentGaps(factory: ConstraintFactory): Constraint {
        return factory.forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal(Lesson::group),
                Joiners.equal { it.timeslot?.dayOfWeek })
            .filter { l1, l2 ->
                val p1 = l1.timeslot?.pairNumber ?: 0
                val p2 = l2.timeslot?.pairNumber ?: 0
                abs(p1 - p2) > 1
            }
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Minimize gaps")
    }
}