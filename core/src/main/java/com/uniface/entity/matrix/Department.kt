package com.uniface.entity.matrix

import jakarta.persistence.*

@Entity
@Table(name = "departments")
class Department(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    val name: String = "", // Masalan: Dasturiy injiniring kafedrasi

    @ManyToOne
    val faculty: Faculty? = null // Qaysi fakultetga qarashliligi
)