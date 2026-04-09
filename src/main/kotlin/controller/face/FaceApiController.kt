package com.uniface.controller.face

import com.uniface.dto.FaceResponse
import com.uniface.service.FaceService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
        @RequestParam("subjectId") subjectId: Long,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("teacherId") teacherId: Long // Ism o'rniga ID kutamiz
    ): FaceResponse {
        // 1. Fayl bo'sh emasligini tekshirish (Safety first!)
        if (file.isEmpty) {
            return FaceResponse(false, "Rasm yuklanmagan!")
        }

        // 2. Service'ga hamma argumentlarni uzatamiz
        return faceService.identifyStudent(
            imageBytes = file.bytes,
            subjectId = subjectId,
            groupId = groupId,
            teacherId = teacherId
        )
    }
}