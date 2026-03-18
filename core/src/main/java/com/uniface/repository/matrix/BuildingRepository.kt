package com.uniface.repository.matrix

import com.uniface.entity.*
import com.uniface.entity.matrix.Building
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BuildingRepository : JpaRepository<Building, Long> {
    fun findByName(name: String): Building?
}