package com.uniface.entity

import jakarta.persistence.*

@Entity
@Table(name = "students")
class Student(
    @Id
    val studentId: String, // student_001 (HEMIS ID bo'lishi mumkin)

    val fullName: String,

    val groupName: String,

    @Column(unique = true)
    val faceId: String? = null // AWS Rekognition qaytargan ID
)
