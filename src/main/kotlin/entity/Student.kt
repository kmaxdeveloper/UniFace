package com.uniface.entity

import jakarta.persistence.*

@Entity
@Table(name = "students")
class Student(
    @Id
    val studentId: String = "",
    val fullName: String = "",
    val groupName: String = "",
    @Column(unique = true)
    val faceId: String? = null
)