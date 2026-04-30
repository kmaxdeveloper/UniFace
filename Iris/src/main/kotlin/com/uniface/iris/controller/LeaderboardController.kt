package com.uniface.iris.controller

import com.uniface.iris.service.IrisService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/leaderboard")
class LeaderboardController(
    private val irisService: IrisService
) {

    @GetMapping("/students")
    fun getTopStudents(@RequestParam(defaultValue = "10") limit: Int): ResponseEntity<List<Map<String, Any>>> {
        return ResponseEntity.ok(irisService.getTopStudents(limit))
    }

    @GetMapping("/teachers")
    fun getTopTeachers(@RequestParam(defaultValue = "10") limit: Int): ResponseEntity<List<Map<String, Any>>> {
        return ResponseEntity.ok(irisService.getTopTeachers(limit))
    }
}
