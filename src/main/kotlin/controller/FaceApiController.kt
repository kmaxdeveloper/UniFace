package com.uniface.controller

import com.uniface.dto.FaceResponse
import com.uniface.service.FaceService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/face")
@CrossOrigin("*")
class FaceApiController(private val faceService: FaceService) {

    @PostMapping("/enroll")
    fun enroll(
        @RequestParam("studentId") id: String,
        @RequestParam("fullName") fullName: String,
        @RequestParam("groupName") groupName: Long,
        @RequestParam("file") file: MultipartFile
    ): FaceResponse {
        return faceService.registerFace(id, fullName, groupName, file.bytes)
    }

    @PostMapping("/verify")
    fun verify(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("subjectId") subjectId: Long,      // Qo'shildi
        @RequestParam("groupId") groupId: Long,          // Qo'shildi
        @RequestParam("teacherName") teacherName: String // Qo'shildi
    ): FaceResponse {
        // Endi hamma argumentlarni service'ga uzatamiz
        return faceService.identifyStudent(
            file.bytes,
            subjectId,
            groupId,
            teacherName
        )
    }
}