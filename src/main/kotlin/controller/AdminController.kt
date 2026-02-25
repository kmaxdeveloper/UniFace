package com.uniface.controller

import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.service.FaceService
import org.springframework.http.ResponseEntity
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
}