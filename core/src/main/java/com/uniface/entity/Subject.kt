package com.uniface.entity

import com.uniface.entity.matrix.Department
import jakarta.persistence.*

@Entity
@Table(name = "subjects")
class Subject() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false, unique = true)
    var name: String = ""

    // UniFace uchun: Masalan "CSE101"
    var code: String? = null

    // Matrix Solver uchun: Jadvalni hisoblashda kerak
    var lectureHours: Int = 0
    var labHours: Int = 0

    @ManyToOne
    var department: Department? = null

    // Hammasini qamrab oluvchi konstruktor
    constructor(
        name: String, code: String? = null, lectureHours: Int = 0, labHours: Int = 0,
        department: Department? = null
    ) : this() {
        this.name = name
        this.code = code
        this.lectureHours = lectureHours
        this.labHours = labHours
        this.department = department
    }
}