package com.uniface.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*

@Entity
@Table(name = "teachers")
class Teacher(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var fullName: String,           // 2. Ism-sharif

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("teacherProfile", "studentProfile", "password")
    var user: User,                 // 3. Login/Parol (User jadvali)

    var department: String = "",    // 7. Kafedra
    var faculty: String = "",       // 7. Fakultet
    var points: Double = 0.0,            // 6. Ballar (Rag'bat)
    var irisLevel: Int = 1,
    var experienceYears: Int = 0,   // Staj

    var status: Boolean = true,     // 8. Holati (Active/Inactive)

    // 4. Fanlari
    @ManyToMany
    @JoinTable(name = "teacher_subjects")
    var subjects: MutableSet<Subject> = mutableSetOf(),

    // --- MANA BU QATORNI QO'SHAMIZ ---
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    @JsonBackReference // JSONda SubjectAllocation ichida Teacher, Teacher ichida yana SubjectAllocation aylanib qolmasligi uchun ✅
    @JsonIgnore
    var allocations: MutableList<SubjectAllocation> = mutableListOf()
)