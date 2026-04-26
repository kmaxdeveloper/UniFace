package com.uniface.config

import com.uniface.entity.SystemLog
import com.uniface.repository.SystemLogRepository
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime

@Aspect
@Component
class LoggingAspect(private val systemLogRepository: SystemLogRepository) {

    @Around("@annotation(loggable)")
    fun logExecution(joinPoint: ProceedingJoinPoint, loggable: Loggable): Any? {
        val start = System.currentTimeMillis()
        var status = "SUCCESS"
        var details = ""

        try {
            val result = joinPoint.proceed()
            details = "Metod muvaffaqiyatli yakunlandi. Parametrlar: ${joinPoint.args.joinToString()}"
            return result
        } catch (e: Exception) {
            status = "FAILURE"
            details = "Xatolik: ${e.message}"
            throw e
        } finally {
            saveLog(loggable, status, details)
        }
    }

    private fun saveLog(loggable: Loggable, status: String, details: String) {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
        val ipAddress = getClientIp(request)
        val auth = SecurityContextHolder.getContext().authentication
        
        val username = auth?.name ?: "ANONYMOUS"
        val role = auth?.authorities?.joinToString { it.authority } ?: "NONE"

        val log = SystemLog(
            username = username,
            role = role,
            action = loggable.action,
            category = loggable.category,
            details = details,
            ipAddress = ipAddress,
            method = request?.method ?: "N/A",
            endpoint = request?.requestURI ?: "N/A",
            status = status,
            timestamp = LocalDateTime.now()
        )

        systemLogRepository.save(log)
    }

    private fun getClientIp(request: HttpServletRequest?): String {
        if (request == null) return "Unknown"
        val remoteAddr = request.getHeader("X-FORWARDED-FOR")
        return if (!remoteAddr.isNullOrBlank()) {
            remoteAddr.split(",")[0]
        } else {
            request.remoteAddr
        }
    }
}
