package com.uniface.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.uniface.entity.matrix.Faculty
import jakarta.persistence.*

@Entity
@Table(name = "student_groups")
class StudentGroup() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false, unique = true)
    var name: String = ""

    // Matrix Solver uchun: Xona sig'imi bilan solishtirishga kerak
    var studentCount: Int = 0
    var course: Int = 1

    // Matrix Solver uchun: Fakultetlararo binolarni taqsimlashga kerak
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    var faculty: Faculty? = null

    // UniFace uchun: Bu guruhga tegishli talabalar
    @JsonIgnore
    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var students: MutableList<Student> = mutableListOf()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patok_id")
    var patok: Patok? = null

    constructor(name: String = "", studentCount: Int = 0, faculty: Faculty? = null) : this() {
        this.name = name
        this.studentCount = studentCount
        this.faculty = faculty
    }
}