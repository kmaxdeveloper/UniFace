package com.uniface.controller.admin

import com.uniface.dto.PatokCreateRequest
import com.uniface.entity.Patok
import com.uniface.service.PatokService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/patok")
class PatokController(private val patokService: PatokService) {

    @GetMapping("/all")
    fun getAll() = ResponseEntity.ok(patokService.getAllPatoks())

    @PostMapping("/create")
    fun create(@RequestBody request: PatokCreateRequest): ResponseEntity<Patok> {
        val patok = patokService.createPatok(request.name, request.groupIds)
        return ResponseEntity.ok(patok)
    }

    @DeleteMapping("/delete/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<String> {
        patokService.deletePatok(id)
        return ResponseEntity.ok("Patok muvaffaqiyatli o'chirildi")
    }
}