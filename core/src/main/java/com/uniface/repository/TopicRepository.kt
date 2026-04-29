package com.uniface.repository

import com.uniface.entity.Topic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TopicRepository : JpaRepository<Topic, Long> {
    fun findBySubjectId(subjectId: Long): List<Topic>
}
