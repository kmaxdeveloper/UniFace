package com.uniface.repository

import com.uniface.entity.Patok
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatokRepository : JpaRepository<Patok, Long>