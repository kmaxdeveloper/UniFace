package com.uniface.service

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
    private val groupRepository: GroupRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
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

        // 3. Fanlarni ID bo'yicha bazadan olib, ustozga biriktiramiz
        if (dto.subjectIds.isNotEmpty()) {
            val subjects = subjectRepository.findAllById(dto.subjectIds)
            teacher.subjects = subjects.toMutableSet()
        }

        // 4. Guruhlarni ID bo'yicha bazadan olib, ustozga biriktiramiz
        if (dto.groupIds.isNotEmpty()) {
            val groups = groupRepository.findAllById(dto.groupIds)
            teacher.groups = groups.toMutableSet()
        }

        // 5. Hammasi tayyor bo'lgach, bittada saqlaymiz
        teacherRepository.save(teacher)
    }

    // 2. Ustoz ma'lumotlarini yangilash (DTO qabul qiladi)
    fun updateUser(id: Long, request: UserDto): User {
        val user = userRepository.findById(id).orElseThrow {
            RuntimeException("Ustoz topilmadi")
        }

        user.fullName = request.fullName

        // Agar yangi parol kelsa va bo'sh bo'lmasa - yangilaymiz
        if (!request.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(request.password)
        }

        return userRepository.save(user)
    }

    fun getAllTeachers(): List<User> {
        // Agar hamma foydalanuvchilar bitta jadvalda bo'lsa, Role orqali filter qilamiz:
        // return userRepository.findAllByRole("TEACHER")

        // Hozircha oddiygina hamma foydalanuvchini qaytarib turamiz (error ketishi uchun):
        return userRepository.findAll()
    }

    @Transactional
    fun updateTeacherFull(userId: Long, dto: TeacherUpdateDto) {
        // 1. Userni topamiz
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("Foydalanuvchi topilmadi!") }

        // 2. User ma'lumotlarini yangilaymiz
        user.username = dto.username ?: user.username
        user.fullName = dto.fullName ?: user.fullName
        if (!dto.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(dto.password)
        }

        // 3. MUHIM: Teacher profilini to'g'ridan-to'g'ri bazadan qidiramiz
        // user.teacherProfile o'rniga teacherRepository dan foydalanamiz
        val teacher = teacherRepository.findByUserId(userId)
            ?: throw Exception("Ushbu userda o'qituvchi profili mavjud emas!")

        // 4. Teacher jadvalidagi ma'lumotlar
        teacher.fullName = dto.fullName ?: teacher.fullName
        teacher.department = dto.department ?: teacher.department
        teacher.faculty = dto.faculty ?: teacher.faculty
        teacher.status = dto.status ?: teacher.status

        // Saqlash
        userRepository.save(user)
        teacherRepository.save(teacher)
    }
}