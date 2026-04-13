package com.uniface.controller.admin

import com.uniface.entity.Curriculum
import com.uniface.repository.CurriculumRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/curriculum")
class CurriculumController(private val repository: CurriculumRepository) {

    @PostMapping
    fun create(@RequestBody curriculum: Curriculum) =
        ResponseEntity.ok(repository.save(curriculum))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody details: Curriculum): ResponseEntity<Curriculum> {
        val existing = repository.findById(id).orElseThrow { Exception("Reja topilmadi") }
        existing.apply {
            subject = details.subject
            group = details.group
            hoursPerWeek = details.hoursPerWeek
            semester = details.semester
        }
        return ResponseEntity.ok(repository.save(existing))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        repository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getCurriculums(
        @RequestParam(required = false) groupId: Long?,
        @RequestParam(required = false) semester: Int?
    ): ResponseEntity<List<Curriculum>> {
        val result = when {
            groupId != null && semester != null ->
                repository.findByGroupIdAndSemester(groupId, semester)
            groupId != null ->
                repository.findByGroupId(groupId)
            semester != null ->
                repository.findBySemester(semester)
            else ->
                repository.findAll()
        }
        return ResponseEntity.ok(result)
    }
}