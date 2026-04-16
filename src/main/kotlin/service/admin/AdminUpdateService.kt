package com.uniface.service.admin

import com.uniface.entity.*
import com.uniface.entity.matrix.*
import com.uniface.repository.*
import com.uniface.repository.matrix.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUpdateService(
    private val roomRepository: RoomRepository,
    private val buildingRepository: BuildingRepository,
    private val departmentRepository: DepartmentRepository,
    private val studentRepository: StudentRepository,
    private val groupRepository: GroupRepository,
    private val subjectRepository: SubjectRepository,
    private val facultyRepository: FacultyRepository
) {

    // 1. FANNI YANGILASH (Matrix Solver uchun eng muhimi)
    @Transactional
    fun updateSubject(id: Long, details: Subject): Subject {
        val subject = subjectRepository.findById(id).orElseThrow { Exception("Fan topilmadi!") }
        subject.name = details.name
        subject.code = details.code
        subject.lectureHours = details.lectureHours
        subject.labHours = details.labHours
        subject.department = details.department
        return subjectRepository.save(subject)
    }

    // 2. GURUHNI YANGILASH
    @Transactional
    fun updateGroup(id: Long, details: StudentGroup): StudentGroup {
        val group = groupRepository.findById(id).orElseThrow { Exception("Guruh topilmadi!") }
        group.name = details.name
        group.course = details.course
        group.faculty = details.faculty
        return groupRepository.save(group)
    }

    // 3. XONANI YANGILASH
    @Transactional
    fun updateRoom(id: Long, details: Room): Room {
        val room = roomRepository.findById(id).orElseThrow { Exception("Xona topilmadi!") }
        room.roomNumber = details.roomNumber
        room.capacity = details.capacity
        room.isLaboratory = details.isLaboratory
        room.building = details.building
        return roomRepository.save(room)
    }

    // 4. BINONI YANGILASH
    @Transactional
    fun updateBuilding(id: Long, details: Building): Building {
        val bino = buildingRepository.findById(id).orElseThrow { Exception("Bino topilmadi!") }
        bino.name = details.name
        return buildingRepository.save(bino)
    }

    // 5. KAFEDRANI YANGILASH
    @Transactional
    fun updateDepartment(id: Long, details: Department): Department {
        val depart = departmentRepository.findById(id).orElseThrow { Exception("Kafedra topilmadi!") }
        depart.name = details.name
        depart.faculty = details.faculty
        return departmentRepository.save(depart)
    }

    // 6. TALABANI YANGILASH
    @Transactional
    fun updateStudent(id: String, fullName: String, groupId: Long): Student {
        val student = studentRepository.findById(id).orElseThrow { Exception("Talaba topilmadi!") }
        val group = groupRepository.findById(groupId).orElseThrow { Exception("Guruh topilmadi!") }
        student.fullName = fullName
        student.group = group
        return studentRepository.save(student)
    }

    // 7. FAKULTETNI YANGILASH (Buni ham qo'shib qo'yamiz)
    @Transactional
    fun updateFaculty(id: Long, details: Faculty): Faculty {
        val faculty = facultyRepository.findById(id).orElseThrow { Exception("Fakultet topilmadi!") }
        faculty.name = details.name
        return facultyRepository.save(faculty)
    }
}