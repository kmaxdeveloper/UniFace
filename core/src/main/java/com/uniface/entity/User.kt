package com.uniface.entity

import com.uniface.data.Role
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    var username: String = "",

    var password: String = "",

    var fullName: String = "",

    @Enumerated(EnumType.STRING)
    var role: Role = Role.ROLE_STUDENT,

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
    var teacherProfile: Teacher? = null,

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
    var studentProfile: Student? = null
)