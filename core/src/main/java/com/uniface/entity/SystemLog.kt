package com.uniface.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "system_logs")
class SystemLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val username: String = "",
    val role: String = "",
    val action: String = "",
    
    @Column(columnDefinition = "TEXT")
    val details: String = "",
    
    val ipAddress: String = "",
    val method: String = "",
    val endpoint: String = "",
    val status: String = "", // SUCCESS, FAILURE
    
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    val category: String = "SYSTEM" // AUTH, DATABASE, SECURITY, SYSTEM
)
