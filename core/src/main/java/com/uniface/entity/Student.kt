package com.uniface.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "students")
class Student(
    @Id
    @Column(name = "student_id")
    var studentId: String = "",

    var fullName: String = "",

    @Column(unique = true)
    var faceId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: StudentGroup? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    var user: User? = null,

    var irisPoints: Int = 0,
    var irisLevel: Int = 1
)