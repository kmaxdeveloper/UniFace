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
    fun rewardForGame(
        principal: Principal,
        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "big") type: String
    ): ResponseEntity<Map<String, Any>> {
        val username = principal.name
        val activity = if (type == "small") IrisActivity.GAME_REWARD_SMALL else IrisActivity.GAME_REWARD_BIG
        
        irisService.addPointsToStudent(username, activity)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Tabriklaymiz! +${activity.points} IRIS ball berildi.",
            "pointsAdded" to activity.points
        ))
    }
}
