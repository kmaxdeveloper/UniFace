package com.uniface.entity

import jakarta.persistence.*

@Entity
@Table(name = "subject_allocations")
class SubjectAllocation() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    var subject: Subject? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    var teacher: Teacher? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") // Guruhga biriktirish
    var group: StudentGroup? = null

    var isPatok: Boolean = false // Agar bu butun patok uchun bo'lsa true
    var patokName: String? = null // Masalan: "940-21 Patok"

    // AllocationService-da qulay foydalanish uchun to'liq konstruktor
    constructor(subject: Subject?, teacher: Teacher?, group: StudentGroup?, isPatok: Boolean = false) : this() {
        this.subject = subject
        this.teacher = teacher
        this.group = group
        this.isPatok = isPatok
    }
}