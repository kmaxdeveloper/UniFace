package com.uniface.service

import com.uniface.config.Loggable

import com.uniface.data.Role
import com.uniface.dto.TeacherDto
import com.uniface.entity.User
import com.uniface.dto.UserDto // Controller'dan keladigan ma'lumotlar
import com.uniface.dto.teacher.TeacherUpdateDto
import com.uniface.entity.Teacher
import com.uniface.repository.GroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.TeacherRepository
import com.uniface.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val teacherRepository: TeacherRepository,
    private val subjectRepository: SubjectRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    @Loggable(action = "CREATE_TEACHER", category = "DATABASE")
    fun saveTeacher(dto: TeacherDto) {
        // 1. Userni yaratamiz
        val user = userRepository.save(User(
            fullName = dto.fullName,
            username = dto.username,
            password = passwordEncoder.encode(dto.password),
            role = Role.ROLE_TEACHER
        ))

        // 2. Teacher obyektini yaratamiz
        val teacher = Teacher(
            fullName = dto.fullName,
            user = user,
            department = dto.department,
            faculty = dto.faculty,
            points = dto.points,
            status = dto.status
        )

        // 3. Domla bera oladigan fanlarni biriktiramiz
        if (dto.subjectIds.isNotEmpty()) {
            val subjects = subjectRepository.findAllById(dto.subjectIds)
            teacher.subjects = subjects.toMutableSet()
        }

        // 4. Saqlash
        teacherRepository.save(teacher)
    }

    @Transactional
    @Loggable(action = "UPDATE_TEACHER", category = "DATABASE")
    fun updateTeacherFull(userId: Long, dto: TeacherUpdateDto) {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("Foydalanuvchi topilmadi!") }

        user.username = dto.username
        user.fullName = dto.fullName
        if (!dto.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(dto.password)
        }

        val teacher = teacherRepository.findByUserId(userId)
            ?: throw Exception("Ushbu userda o'qituvchi profili mavjud emas!")

        teacher.fullName = dto.fullName
        teacher.department = dto.department
        teacher.faculty = dto.faculty
        teacher.status = dto.status

        // Fanlarni ham yangilab qo'yamiz (agar yangi ro'yxat kelsa)
        if (dto.subjectIds.isNotEmpty()) {
            val subjects = subjectRepository.findAllById(dto.subjectIds)
            teacher.subjects = subjects.toMutableSet()
        }

        userRepository.save(user)
        teacherRepository.save(teacher)
    }

    @Transactional // Bu juda muhim!
    fun getAllTeachers(): List<Teacher> {
        val teachers = teacherRepository.findAll()
        // Har bir o'qituvchining fanlarini "o'yg'otib" (initialize) qo'yamiz
        teachers.forEach {
            it.subjects.size
        }
        return teachers
    }

    @Transactional
    fun updateUser(id: Long, request: UserDto): User {
        val user = userRepository.findById(id).orElseThrow { RuntimeException("Foydalanuvchi topilmadi") }
        user.fullName = request.fullName
        if (!request.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(request.password)
        }
        return userRepository.save(user)
    }

    // sonya blits
}