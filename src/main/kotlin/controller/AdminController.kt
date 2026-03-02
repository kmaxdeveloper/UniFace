package com.uniface.controller

import com.uniface.dto.UserDto
import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.service.FaceService
import com.uniface.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val faceService: FaceService,
    private val subjectRepo: SubjectRepository,
    private val groupRepo: StudentGroupRepository
) {
    // Guruhlar va Fanlarni boshqarish
    @PostMapping("/groups")
    fun addGroup(@RequestBody group: StudentGroup) = ResponseEntity.ok(groupRepo.save(group))

    @GetMapping("/groups")
    fun getGroups() = ResponseEntity.ok(groupRepo.findAll())

    @PostMapping("/subjects")
    fun addSubject(@RequestBody subject: Subject) = ResponseEntity.ok(subjectRepo.save(subject))

    @GetMapping("/subjects")
    fun getSubjects() = ResponseEntity.ok(subjectRepo.findAll())

    // Talabani rasmga olib ro'yxatdan o'tkazish
    @PostMapping("/student/register")
    fun registerStudent(
        @RequestParam("id") id: String,
        @RequestParam("fullName") fullName: String,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("image") file: MultipartFile
    ) = ResponseEntity.ok(faceService.registerFace(id, fullName, groupId, file.bytes))

    @RestController
    @RequestMapping("/api/v1/admin")
    class AdminController(
        private val userService: UserService,
        private val passwordEncoder: PasswordEncoder
    ) {

        // Yangi ustoz qo'shish
        @PostMapping("/add-teacher")
        fun addTeacher(@RequestBody request: UserDto): ResponseEntity<String> {
            userService.saveTeacher(request)
            return ResponseEntity.ok("Ustoz muvaffaqiyatli qo'shildi!")
        }

        // Ustoz ma'lumotlarini yoki parolini o'zgartirish
        @PutMapping("/update-teacher/{id}")
        fun updateTeacher(@PathVariable id: Long, @RequestBody request: UserDto): ResponseEntity<String> {
            userService.updateUser(id, request)
            return ResponseEntity.ok("Ma'lumotlar yangilandi!")
        }
    }
}