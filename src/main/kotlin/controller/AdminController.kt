package com.uniface.controller

import com.uniface.dto.UserDto
import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.service.FaceService
import com.uniface.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val faceService: FaceService,
    private val userService: UserService, // ✅ UserService qo'shildi
    private val subjectRepo: SubjectRepository,
    private val groupRepo: StudentGroupRepository
) {

    // --- 1. GURUHLARNI BOSHQARISH ---
    @PostMapping("/groups")
    fun addGroup(@RequestBody group: StudentGroup) = ResponseEntity.ok(groupRepo.save(group))

    @GetMapping("/groups")
    fun getGroups() = ResponseEntity.ok(groupRepo.findAll())

    // --- 2. FANLARNI BOSHQARISH ---
    @PostMapping("/subjects")
    fun addSubject(@RequestBody subject: Subject) = ResponseEntity.ok(subjectRepo.save(subject))

    @GetMapping("/subjects")
    fun getSubjects() = ResponseEntity.ok(subjectRepo.findAll())

    // --- 3. TALABANI RO'YXATDAN O'TKAZISH (AWS + DB) ---
    @PostMapping("/student/register")
    fun registerStudent(
        @RequestParam("id") id: String,
        @RequestParam("fullName") fullName: String,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("image") file: MultipartFile
    ): ResponseEntity<*> {
        return try {
            val response = faceService.registerFace(id, fullName, groupId, file.bytes)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    // --- 4. USTOZLARNI BOSHQARISH ---
    @PostMapping("/add-teacher")
    fun addTeacher(@RequestBody request: UserDto): ResponseEntity<String> {
        return try {
            userService.saveTeacher(request)
            ResponseEntity.ok("Ustoz muvaffaqiyatli qo'shildi!")
        } catch (e: RuntimeException) {
            ResponseEntity.badRequest().body(e.message) // Masalan: "Bu login band..."
        }
    }

    @PutMapping("/update-teacher/{id}")
    fun updateTeacher(@PathVariable id: Long, @RequestBody request: UserDto): ResponseEntity<String> {
        return try {
            userService.updateUser(id, request)
            ResponseEntity.ok("Ma'lumotlar yangilandi!")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
}