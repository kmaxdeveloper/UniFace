package com.uniface.repository.matrix

import com.uniface.entity.matrix.Building
import com.uniface.entity.matrix.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository : JpaRepository<Room, Long> {
    // BuildingId emas, Building obyektining o'zi orqali qidiramiz
    fun findByRoomNumberAndBuilding(roomNumber: String, building: Building): Room?

    fun findAllByBuildingName(buildingName: String): List<Room>
    fun findByRoomNumber(roomNumber: String): Room?
}