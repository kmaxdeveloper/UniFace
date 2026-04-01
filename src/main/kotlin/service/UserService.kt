package com.uniface.service

import com.uniface.data.Role
import com.uniface.dto.TeacherDto
import com.uniface.entity.User
import com.uniface.dto.UserDto // Controller'dan keladigan ma'lumotlar
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
}