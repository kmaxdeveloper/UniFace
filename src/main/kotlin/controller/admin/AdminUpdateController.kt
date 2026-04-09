package com.uniface.controller.admin

import com.uniface.dto.teacher.TeacherUpdateDto
import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.entity.matrix.Building
import com.uniface.entity.matrix.Department
import com.uniface.entity.matrix.Faculty
import com.uniface.entity.matrix.Room
import com.uniface.repository.GroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.matrix.FacultyRepository
import com.uniface.service.UserService
import com.uniface.service.matrix.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class AdminUpdateController(
    private val userService: UserService,
    private val subjectRepository: SubjectRepository,
    private val groupRepository: GroupRepository,
    private val facultyRepository: FacultyRepository,
    private val adminService: AdminService
) {

    @PutMapping("/update-teacher/{id}")
    fun updateTeacher(@PathVariable id: Long, @RequestBody request: TeacherUpdateDto) = try {
        userService.updateTeacherFull(id, request)
        ResponseEntity.ok("O'qituvchi ma'lumotlari yangilandi!")
    } catch (e: Exception) {
        ResponseEntity.badRequest().body("Yangilashda xato: ${e.message}")
    }

    // 2. GURUHNI YANGILASH (Nomi yoki fakultetini o'zgartirish)
    @PutMapping("/update-group/{id}")
    fun updateGroup(@PathVariable id: Long, @RequestBody groupDetails: StudentGroup) = try {
        val group = groupRepository.findById(id).orElseThrow { Exception("Guruh topilmadi!") }
        group.name = groupDetails.name
        // Agar boshqa maydonlar bo'lsa, ularni ham shu yerda update qilamiz
        ResponseEntity.ok(groupRepository.save(group))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }

    // 3. FANNi YANGILASH
    @PutMapping("/update-subject/{id}")
    fun updateSubject(@PathVariable id: Long, @RequestBody subjectDetails: Subject) = try {
        val subject = subjectRepository.findById(id).orElseThrow { Exception("Fan topilmadi!") }
        subject.name = subjectDetails.name
        ResponseEntity.ok(subjectRepository.save(subject))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }

    // 4. FAKULTETNI YANGILASH
    @PutMapping("/update-faculty/{id}")
    fun updateFaculty(@PathVariable id: Long, @RequestBody facultyDetails: Faculty) = try {
        val faculty = facultyRepository.findById(id).orElseThrow { Exception("Fakultet topilmadi!") }
        faculty.name = facultyDetails.name
        ResponseEntity.ok(facultyRepository.save(faculty))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }

    @PutMapping("/update-room/{id}")
    fun updateRoom(@PathVariable id: Long, @RequestBody roomDetails: Room): ResponseEntity<*> {
        return try {
            val updatedRoom = adminService.updateRoom(id, roomDetails)
            ResponseEntity.ok(updatedRoom)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Xatolik: ${e.message}")
        }
    }

    // 6. Bino yangilash
    @PutMapping("/update-building/{id}")
    fun updateBuilding(@PathVariable id: Long, @RequestBody details: Building) = try {
        ResponseEntity.ok(adminService.updateBuilding(id, details))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }

    // 7. Kafedra yangilash
    @PutMapping("/update-depart/{id}")
    fun updateDepartment(@PathVariable id: Long, @RequestBody details: Department) = try {
        ResponseEntity.ok(adminService.updateDepartment(id, details))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }

    // 8. Talaba ma'lumotlarini yangilash (ID: String)
    @PutMapping("/update-student/{id}")
    fun updateStudent(
        @PathVariable id: String, // Long emas, String!
        @RequestParam fullName: String,
        @RequestParam groupId: Long
    ) = try {
        ResponseEntity.ok(adminService.updateStudent(id, fullName, groupId))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body("Talabani yangilashda xato: ${e.message}")
    }

    // Kelajakda talaba yoki boshqa ma'lumotlarni update qilish kodi ham shu yerga tushadi
}