package com.uniface.iris.controller

import com.uniface.iris.model.IrisActivity
import com.uniface.iris.service.IrisService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/v1/iris/games")
class GameController(
    private val irisService: IrisService
) {

    // Talabalar o'yin o'ynab bo'lgach ball olishi uchun
    @PostMapping("/reward")
    fun rewardForGame(principal: Principal): ResponseEntity<Map<String, Any>> {
        val username = principal.name
        
        // TODO: Kelajakda kunlik limit qo'shish (masalan, kunda max 5 marta ball olish mumkin)
        irisService.addPointsToStudent(username, IrisActivity.GAME_REWARD)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Tabriklaymiz! +2 IRIS ball berildi.",
            "pointsAdded" to 2
        ))
    }
}
