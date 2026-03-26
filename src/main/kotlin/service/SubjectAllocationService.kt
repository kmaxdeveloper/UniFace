package com.uniface.service

import com.uniface.entity.SubjectAllocation
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectAllocationRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.TeacherRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SubjectAllocationService(
    private val allocationRepository: SubjectAllocationRepository,
    private val subjectRepository: SubjectRepository,
    private val teacherRepository: TeacherRepository,
    private val groupRepository: StudentGroupRepository
) {

    @Transactional
    fun createAllocation(
        subjectId: Long,
        teacherId: Long,
        groupId: Long? = null,
        patokName: String? = null,
        isPatok: Boolean = false
    ): String {
        // 1. Obyektlarni bazadan tekshirib olamiz
        val subject = subjectRepository.findById(subjectId)
            .orElseThrow { Exception("Fan (Subject) topilmadi!") }
        val teacher = teacherRepository.findById(teacherId)
            .orElseThrow { Exception("O'qituvchi topilmadi!") }

        // 2. Yangi bog'lanish yaratamiz
        val allocation = SubjectAllocation().apply {
            this.subject = subject
            this.teacher = teacher
            this.isPatok = isPatok

            if (isPatok) {
                // Agar patok bo'lsa, guruh shart emas, patok nomi muhim
                this.patokName = patokName ?: "Noma'lum Patok"
                this.group = null
            } else {
                // Agar alohida guruh bo'lsa
                this.group = groupId?.let {
                    groupRepository.findById(it).orElseThrow { Exception("Guruh topilmadi!") }
                }
                this.patokName = null
            }
        }

        allocationRepository.save(allocation)
        return "Muvaffaqiyatli bog'landi: ${subject.name} -> ${teacher.fullName}"
    }

    // Ustoz o'ziga tegishli guruhlarni ko'rishi uchun
    fun getTeacherLessons(teacherId: Long) = allocationRepository.findAllByTeacherId(teacherId)
}