package com.uniface.entity

import jakarta.persistence.*

@Entity
class Curriculum(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne var subject: Subject? = null,
    @ManyToOne var group: StudentGroup? = null,

    var hoursPerWeek: Int = 2, // Haftasiga necha soat (para) dars bo'lishi
    var semester: Int = 1      // Qaysi semestr uchunligi
)