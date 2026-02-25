package com.uniface.entity

import jakarta.persistence.*

@Entity
@Table(name = "subjects")
class Subject() { // Bo'sh konstruktor uchun
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false, unique = true)
    var name: String = ""

    var code: String? = null

    constructor(name: String, code: String? = null) : this() {
        this.name = name
        this.code = code
    }
}