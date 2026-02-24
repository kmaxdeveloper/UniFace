package com.uniface.controller

import com.uniface.service.FaceService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/uniface")
class StudentController(private val faceService: FaceService) {

//    @PostMapping("/register")
//    fun register(@RequestParam("id") id: String, @RequestParam("image") file: MultipartFile): String {
//        return faceService.registerFace(id, file.bytes)
//    }
//
//    @GetMapping("/setup")
//    fun setup(): String {
//        return faceService.createCollection()
//    }
//
//    @PostMapping("/identify") // Aynan POST ekanligini tekshir
//    fun identify(@RequestParam("image") file: MultipartFile): String {
//        return faceService.identifyStudent(file.bytes)
//    }
}