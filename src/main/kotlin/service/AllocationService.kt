package com.uniface.service

import com.uniface.entity.SubjectAllocation
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectAllocationRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.TeacherRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AllocationService(
    private val allocationRepository: SubjectAllocationRepository,
    private val subjectRepository: SubjectRepository,
    private val teacherRepository: TeacherRepository,
    private val groupRepository: StudentGroupRepository
) {
    @Transactional
    fun allocateSubject(
        subjectId: Long,
        teacherId: Long,
        groupId: Long?, // Agar patok bo'lsa null bo'lishi mumkin
        isPatok: Boolean,
        patokName: String?
    ): String {
        val subject = subjectRepository.findById(subjectId).orElseThrow { Exception("Fan topilmadi") }
        val teacher = teacherRepository.findById(teacherId).orElseThrow { Exception("Ustoz topilmadi") }

        val allocation = SubjectAllocation().apply {
            this.subject = subject
            this.teacher = teacher
            this.isPatok = isPatok
            this.patokName = patokName

            if (!isPatok && groupId != null) {
                this.group = groupRepository.findById(groupId).orElse(null)
            }
        }

        allocationRepository.save(allocation)
        return "Muvaffaqiyatli biriktirildi!"
    }
}