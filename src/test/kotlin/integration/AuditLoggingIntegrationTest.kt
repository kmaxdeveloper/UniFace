package com.uniface.integration

import com.uniface.config.Loggable
import com.uniface.repository.SystemLogRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles

@Service
class TestService {
    @Loggable(action = "TEST_ACTION", category = "TEST_CAT")
    fun performAction() {
        // Shunchaki test amali
    }
}

@SpringBootTest
@ActiveProfiles("test")
class AuditLoggingIntegrationTest {

    @Autowired
    lateinit var testService: TestService

    @Autowired
    lateinit var systemLogRepository: SystemLogRepository

    @Test
    fun `should save log when loggable method is called`() {
        // 1. Dastlab loglar sonini tekshiramiz
        val initialCount = systemLogRepository.count()

        // 2. Loggable metodni chaqiramiz
        testService.performAction()

        // 3. Log saqlanganini tekshiramiz
        val finalCount = systemLogRepository.count()
        assertEquals(initialCount + 1, finalCount, "Log saqlanmadi!")

        val lastLog = systemLogRepository.findAll().last()
        assertEquals("TEST_ACTION", lastLog.action)
        assertEquals("TEST_CAT", lastLog.category)
    }
}
