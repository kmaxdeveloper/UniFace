package com.uniface.iris.service

import com.uniface.entity.Student
import com.uniface.entity.Teacher
import com.uniface.iris.model.IrisActivity
import com.uniface.repository.StudentRepository
import com.uniface.repository.TeacherRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IrisService(
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository
) {

    @Transactional
    fun addPointsToStudent(username: String, activity: IrisActivity) {
        val student = studentRepository.findByUserUsername(username) ?: return
        student.irisPoints += activity.points
        
        val newLevel = calculateLevel(student.irisPoints)
        if (newLevel > student.irisLevel) {
            student.irisLevel = newLevel
        }
        
        studentRepository.save(student)
    }

    @Transactional
    fun addPointsToTeacher(username: String, activity: IrisActivity) {
        val teacher = teacherRepository.findByUserUsername(username) ?: return
        teacher.points += activity.points
        
        val newLevel = calculateTeacherLevel(teacher.points)
        if (newLevel > teacher.irisLevel) {
            teacher.irisLevel = newLevel
        }
        
        teacherRepository.save(teacher)
    }

    private fun calculateLevel(points: Double): Int {
        return when {
            points < 2000.0 -> (points / 200.0).toInt() + 1
            else -> {
                val baseLevel = 10
                val remainingPoints = points - 2000.0
                val addedLevels = (remainingPoints / 1000.0).toInt()
                (baseLevel + addedLevels).coerceAtMost(20)
            }
        }
    }

    private fun calculateTeacherLevel(points: Double): Int {
        return when {
            points < 500.0 -> 1
            points < 1500.0 -> 2
            points < 4000.0 -> 3
            points < 10000.0 -> 4
            else -> 5
        }
    }

    fun getStudentLevelName(level: Int): String {
        return when {
            level <= 5 -> "Junior Student"
            level <= 10 -> "Middle Student"
            level <= 15 -> "Senior Student"
            level <= 18 -> "Lead Student"
            level == 19 -> "Staff Student"
            else -> "Principal Student"
        }
    }

    fun getTeacherLevelName(level: Int, experience: Int): String {
        if (experience >= 30) return "Ustoz Shifu"
        if (experience >= 25) return "Guru"
        
        return when (level) {
            1 -> "Junior Sensei"
            2 -> "Middle Sensei"
            3 -> "Senior Sensei"
            4 -> "Principal Sensei"
            else -> "Architect Sensei"
        }
    }

    fun getTopStudents(limit: Int): List<Map<String, Any>> {
        return studentRepository.findAll()
            .sortedByDescending { it.irisPoints }
            .take(limit)
            .map {
                mapOf(
                    "fullName" to it.fullName,
                    "points" to it.irisPoints,
                    "level" to it.irisLevel,
                    "levelName" to getStudentLevelName(it.irisLevel),
                    "group" to (it.group?.name ?: "")
                )
            }
    }

    fun getTopTeachers(limit: Int): List<Map<String, Any>> {
        return teacherRepository.findAll()
            .sortedByDescending { it.points }
            .take(limit)
            .map {
                mapOf(
                    "fullName" to it.fullName,
                    "points" to it.points,
                    "level" to it.irisLevel,
                    "levelName" to getTeacherLevelName(it.irisLevel, it.experienceYears),
                    "department" to it.department
                )
            }
    }
}
