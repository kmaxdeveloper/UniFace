package com.uniface.service.matrix

import com.uniface.entity.matrix.Building
import com.uniface.entity.matrix.Room
import com.uniface.repository.matrix.BuildingRepository
import com.uniface.repository.matrix.RoomRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RoomEntryService(
    private val roomRepository: RoomRepository,
    private val buildingRepository: BuildingRepository
) {
    @Transactional
    fun saveRoom(buildingName: String, roomNumber: String, capacity: Int, isLab: Boolean): Room {
        // 1. Binoni qidiramiz yoki yaratamiz
        val building = buildingRepository.findByName(buildingName)
            ?: buildingRepository.save(Building().apply { this.name = buildingName })

        // 2. To'g'rilangan metod: building obyektini o'zini beramiz
        val existingRoom = roomRepository.findByRoomNumberAndBuilding(roomNumber, building)

        if (existingRoom != null) return existingRoom

        // 3. Yangi xonani saqlash
        val newRoom = Room().apply {
            this.roomNumber = roomNumber
            this.capacity = capacity
            this.isLaboratory = isLab
            this.building = building
        }

        return roomRepository.save(newRoom)
    }
}