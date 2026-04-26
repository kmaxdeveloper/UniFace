package com.uniface.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "system_logs")
class SystemLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var username: String = "",
    var role: String = "",
    var action: String = "",
    
    @Column(columnDefinition = "TEXT")
    var details: String = "",
    
    var ipAddress: String = "",
    var method: String = "",
    var endpoint: String = "",
    var status: String = "",
    
    var timestamp: LocalDateTime = LocalDateTime.now(),
    
    var category: String = "SYSTEM"
)
