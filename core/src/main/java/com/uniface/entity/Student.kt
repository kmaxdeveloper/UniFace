package com.uniface.entity

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: StudentGroup? = null

    constructor(studentId: String, fullName: String, faceId: String, group: StudentGroup?) : this() {
        this.studentId = studentId
        this.fullName = fullName
        this.faceId = faceId
        this.group = group
    }
}