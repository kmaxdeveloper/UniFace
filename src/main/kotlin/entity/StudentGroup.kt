package com.uniface.entity

import jakarta.persistence.*

@Entity
@Table(name = "student_groups")
class StudentGroup() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false, unique = true)
    var name: String = ""

    constructor(name: String) : this() {
        this.name = name
    }
}