package com.uniface.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "patoklar")
class Patok(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,

    @OneToMany(mappedBy = "patok", fetch = FetchType.LAZY)
    @JsonIgnore
    var groups: MutableList<StudentGroup> = mutableListOf()
)