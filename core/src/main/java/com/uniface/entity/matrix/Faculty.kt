package com.uniface.entity.matrix

import jakarta.persistence.*

@Entity
@Table(name = "faculties")
class Faculty(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    val name: String = "" // Masalan: AKT fakulteti
)