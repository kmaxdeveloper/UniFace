package com.uniface.entity.matrix

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*

@Entity
@Table(name = "departments")
class Department(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    var name: String = "", // Masalan: Dasturiy injiniring kafedrasi

    @ManyToOne(fetch = FetchType.LAZY) // Faqat kerak bo'lgandagina yuklanadi
    @JoinColumn(name = "faculty_id")
    //@JsonIgnoreProperties("departments")
    var faculty: Faculty? = null // Qaysi fakultetga qarashliligi
)