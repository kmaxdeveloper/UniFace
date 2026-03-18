package com.uniface.matrix.util

import com.uniface.entity.matrix.Lesson
import com.uniface.entity.matrix.Room
import com.uniface.matrix.domain.*
import java.time.DayOfWeek

object MatrixDataGenerator {

//    fun generateTestTimetable(): Timetable {
//        val timeslots = TatuScheduleFactory.getSlots()
//
//        // 1. Xonalar (Rooms) - A va B bino
//        val rooms = listOf(
//            Room(1, "101-xona", "A-bino", 30),
//            Room(2, "102-xona", "A-bino", 25),
//            Room(3, "201-xona", "B-bino", 40), // Katta xona
//            Room(4, "202-xona", "B-bino", 20)
//        )
//
//        // 2. Darslar (Lessons) - Planning Entities
//        // Eslatma: timeSlot va room hozircha null, ularni Solver topishi kerak
//        val lessons = mutableListOf<Lesson>()
//
//        // 1-guruh: 611-21 (Software Engineering)
//        lessons.add(Lesson(1, "Ma'lumotlar strukturasi", "E. Karimov", "611-21", 28, 1))
//        lessons.add(Lesson(2, "Algoritmlar", "E. Karimov", "611-21", 28, 1))
//        lessons.add(Lesson(3, "Android Development", "A. Alimov", "611-21", 28, 1))
//
//        // 2-guruh: 612-21
//        lessons.add(Lesson(4, "Ma'lumotlar strukturasi", "E. Karimov", "612-21", 22, 1)) // Teacher conflict ehtimoli
//        lessons.add(Lesson(5, "Sun'iy intellekt", "D. Toirov", "612-21", 22, 1))
//        lessons.add(Lesson(6, "Kiberxavfsizlik", "B. Bobojonov", "612-21", 22, 1))
//
//        // 3-guruh: 710-20 (Katta guruh, B-binoda o'qishi kerak)
//        lessons.add(Lesson(7, "Oliy matematika", "S. Soliev", "710-20", 38, 2))
//        lessons.add(Lesson(8, "Diskret tuzilmalar", "S. Soliev", "710-20", 38, 2))
//
//        return Timetable(timeslots, rooms, lessons)
//    }
}