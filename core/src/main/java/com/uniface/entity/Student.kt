package com.uniface.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "students")
class Student() {
    @Id
    @Column(name = "student_id")
    var studentId: String = ""

    var fullName: String = ""

    @Column(unique = true)
    var faceId: String = ""

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Login tizimi bilan ulanish
    @JsonBackReference
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: StudentGroup? = null

    constructor(studentId: String, fullName: String, faceId: String, group: StudentGroup?, user: User?) : this() {
        this.studentId = studentId
        this.fullName = fullName
        this.faceId = faceId
        this.group = group
        this.user = user
    }
}