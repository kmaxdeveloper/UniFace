package com.uniface.matrix.service

import com.uniface.entity.SubjectAllocation
import com.uniface.repository.CurriculumRepository
import com.uniface.repository.SubjectAllocationRepository
import com.uniface.repository.TeacherRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SubjectAllocationService2(
    private val curriculumRepository: CurriculumRepository,
    private val teacherRepository: TeacherRepository,
    private val allocationRepository: SubjectAllocationRepository
) {

    @Transactional
    fun autoAllocateTeachers() {
        val curriculums = curriculumRepository.findAll()
        val teachers = teacherRepository.findAll()

        curriculums.forEach { plan ->
            // Oddiy logika: Shu fanga ixtisoslashgan o'qituvchini topish
            // Yoki shunchaki yuklamasi kamroq ustozni biriktirish
            val bestTeacher = teachers.filter { it.status == true }
                .minByOrNull { it.allocations.size } // Yuklamasi eng kam ustoz

            val allocation = SubjectAllocation(
                subject = plan.subject!!,
                teacher = bestTeacher!!,
                group = plan.group,
                isPatok = false // Agar guruhlar soni ko'p bo'lsa, true qilsa bo'ladi
            )
            allocationRepository.save(allocation)
        }
    }
}