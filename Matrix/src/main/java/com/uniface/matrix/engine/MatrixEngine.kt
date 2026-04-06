package com.uniface.matrix.engine

import com.uniface.entity.*
import com.uniface.entity.matrix.*
import com.uniface.data.LessonType
import org.springframework.stereotype.Component

@Component
class MatrixEngine {

    fun generate(
        allocations: List<SubjectAllocation>,
        rooms: List<Room>,
        slots: List<TimeSlot>
    ): List<Lesson> {
        val finalLessons = mutableListOf<Lesson>()

        for (alloc in allocations) {
            // 1. Har bir allocation uchun bitta xona va vaqt topamiz
            val foundSlot = findSlot(alloc, rooms, slots, finalLessons)

            if (foundSlot != null) {
                finalLessons.add(foundSlot)
            }
        }
        return finalLessons
    }

    private fun findSlot(
        alloc: SubjectAllocation,
        rooms: List<Room>,
        slots: List<TimeSlot>,
        currentLessons: List<Lesson>
    ): Lesson? {
        for (slot in slots) {
            for (room in rooms) {
                // Cheklovlarni tekshiramiz
                if (isOk(alloc, slot, room, currentLessons)) {
                    return Lesson().apply {
                        this.subject = alloc.subject
                        this.teacher = alloc.teacher
                        this.timeslot = slot
                        this.room = room
                        this.type = LessonType.PRACTICE // Default holatda
                        this.groups = mutableSetOf(alloc.group!!) // Guruhni qo'shamiz
                    }
                }
            }
        }
        return null
    }

    private fun isOk(alloc: SubjectAllocation, slot: TimeSlot, room: Room, lessons: List<Lesson>): Boolean {
        // 1. Xona bo'shmi?
        if (lessons.any { it.room?.id == room.id && it.timeslot?.id == slot.id }) return false

        // 2. O'qituvchi bo'shmi?
        if (lessons.any { it.teacher?.id == alloc.teacher?.id && it.timeslot?.id == slot.id }) return false

        // 3. Guruh bo'shmi?
        if (lessons.any { it.groups.any { g -> g.id == alloc.group?.id } && it.timeslot?.id == slot.id }) return false

        // 4. Sig'im yetadimi?
        if (room.capacity < (alloc.group?.studentCount ?: 0)) return false

        return true
    }
}