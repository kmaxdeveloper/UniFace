package com.uniface.entity

import jakarta.persistence.*

@Entity
@Table(name = "teachers")
class Teacher(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var fullName: String,

    @OneToOne
    @JoinColumn(name = "user_id") // User jadvali bilan ulaymiz
    var user: User? = null,

    var department: String = "",

    // Ustozga biriktirilgan fanlar (Many-to-Many)
    @ManyToMany
    @JoinTable(
        name = "teacher_subjects",
        joinColumns = [JoinColumn(name = "teacher_id")],
        inverseJoinColumns = [JoinColumn(name = "subject_id")]
    )
    var subjects: MutableSet<Subject> = mutableSetOf(),

    // Ustozga biriktirilgan guruhlar (Many-to-Many)
    @ManyToMany
    @JoinTable(
        name = "teacher_groups",
        joinColumns = [JoinColumn(name = "teacher_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")]
    )
    var groups: MutableSet<StudentGroup> = mutableSetOf()
)