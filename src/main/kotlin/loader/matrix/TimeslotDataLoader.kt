package com.uniface.loader.matrix

import com.uniface.entity.matrix.TimeSlot // Repo nomi bilan mosligini tekshir
import com.uniface.repository.matrix.TimeslotRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.DayOfWeek
import java.time.LocalTime

@Configuration
class TimeslotDataLoader {

    @Bean
    fun seedTimeslots(repository: TimeslotRepository): CommandLineRunner {
        return CommandLineRunner {
            if (repository.count() == 0L) {
                val days = listOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
                )

                // Pair: (Boshlanishi, Tugashi) -> Para raqami index orqali olinadi
                val schedule = listOf(
                    LocalTime.of(8, 30) to LocalTime.of(9, 50),   // 1-para
                    LocalTime.of(10, 0) to LocalTime.of(11, 20),  // 2-para
                    LocalTime.of(11, 30) to LocalTime.of(12, 50), // 3-para
                    LocalTime.of(13, 30) to LocalTime.of(14, 50), // 4-para
                    LocalTime.of(15, 0) to LocalTime.of(16, 20),  // 5-para
                    LocalTime.of(16, 30) to LocalTime.of(17, 50)  // 6-para
                )

                val timeslots = days.flatMap { day ->
                    schedule.mapIndexed { index, (start, end) ->
                        TimeSlot(
                            dayOfWeek = day,
                            startTime = start,
                            endTime = end,
                            pairNumber = index + 1 // 1 dan boshlab para raqamini beramiz ✅
                        )
                    }
                }
                repository.saveAll(timeslots)
                println("✅ Matrix: ${timeslots.size} ta TimeSlot muvaffaqiyatli yaratildi!")
            }
        }
    }
}