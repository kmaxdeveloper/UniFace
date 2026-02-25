package com.uniface.controller

import com.uniface.service.FaceService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/uniface")
class StudentController(private val faceService: FaceService) {

    @PostMapping("/register")
    fun register(
        @RequestParam("id") id: String,
        @RequestParam("fullName") fullName: String,    // Buni qo'shdik
        @RequestParam("groupName") groupName: String,  // Buni qo'shdik
        @RequestParam("image") file: MultipartFile
    ): Any {
        // FaceService.registerFace(id, fullName, groupName, imageBytes) shaklida bo'lishi kerak
        return faceService.registerFace(id, fullName, groupName, file.bytes)
    }

    @GetMapping("/setup")
    fun setup(): Any {
        return faceService.createCollection()
    }

    @PostMapping("/identify")
    fun identify(@RequestParam("image") file: MultipartFile): Any {
        // FaceService.identifyStudent(imageBytes)
        return faceService.identifyStudent(file.bytes)
    }
}