package com.uniface.entity.matrix

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "buildings")
data class Building(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var name: String = "", // Masalan: "A-bino", "B-bino", "ARM"
    val floorCount: Int = 0,
    val description: String? = null
)