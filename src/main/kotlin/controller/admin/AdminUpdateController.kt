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
import com.uniface.service.admin.AdminUpdateService
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
    private val adminService: AdminService,
    private val adminUpdateService: AdminUpdateService
) {

    @PutMapping("/update-teacher/{id}")
    fun updateTeacher(@PathVariable id: Long, @RequestBody request: TeacherUpdateDto) = try {
        userService.updateTeacherFull(id, request)
        ResponseEntity.ok("O'qituvchi ma'lumotlari yangilandi!")
    } catch (e: Exception) {
        ResponseEntity.badRequest().body("Yangilashda xato: ${e.message}")
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

    // Controller ichidagi funksiyalar:

    @PutMapping("/update-subject/{id}")
    fun updateSubject(@PathVariable id: Long, @RequestBody details: Subject) = try {
        ResponseEntity.ok(adminUpdateService.updateSubject(id, details))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }

    @PutMapping("/update-group/{id}")
    fun updateGroup(@PathVariable id: Long, @RequestBody details: StudentGroup) = try {
        ResponseEntity.ok(adminUpdateService.updateGroup(id, details))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }

    @PutMapping("/update-faculty/{id}")
    fun updateFaculty(@PathVariable id: Long, @RequestBody details: Faculty) = try {
        ResponseEntity.ok(adminUpdateService.updateFaculty(id, details))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }

    // Kelajakda talaba yoki boshqa ma'lumotlarni update qilish kodi ham shu yerga tushadi
    @PutMapping("/update-topic/{id}")
    fun updateTopic(
        @PathVariable id: Long,
        @RequestParam title: String,
        @RequestParam(required = false) description: String?
    ) = try {
        ResponseEntity.ok(adminService.updateTopic(id, title, description))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message)
    }
}