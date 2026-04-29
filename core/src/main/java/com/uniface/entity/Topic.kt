package com.uniface.entity

import jakarta.persistence.*

@Entity
@Table(name = "topics")
class Topic() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false)
    var title: String = ""

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    var subject: Subject? = null

    constructor(title: String, subject: Subject?, description: String? = null) : this() {
        this.title = title
        this.subject = subject
        this.description = description
    }
}
