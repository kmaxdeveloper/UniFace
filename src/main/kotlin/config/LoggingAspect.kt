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

    @Around("@annotation(com.uniface.config.Loggable) || execution(* com.uniface.service.*.*(..))")
    fun logExecution(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val loggable = signature.method.getAnnotation(Loggable::class.java)
        
        val start = System.currentTimeMillis()
        var status = "SUCCESS"
        var details = ""

        // Agar annotatsiya bo'lmasa, metod nomidan action yasaymiz
        val actionName = loggable?.action ?: signature.method.name.uppercase()
        val categoryName = loggable?.category ?: "SERVICE"

        try {
            val result = joinPoint.proceed()
            details = "Metod muvaffaqiyatli yakunlandi. Parametrlar: ${joinPoint.args.joinToString()}"
            return result
        } catch (e: Exception) {
            status = "FAILURE"
            details = "Xatolik: ${e.message}"
            throw e
        } finally {
            saveLog(joinPoint, actionName, categoryName, status, details)
        }
    }

    private fun saveLog(joinPoint: ProceedingJoinPoint, action: String, category: String, status: String, details: String) {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
        val ipAddress = getClientIp(request)
        val auth = SecurityContextHolder.getContext().authentication
        
        var username = auth?.name ?: "ANONYMOUS"
        var role = auth?.authorities?.joinToString { it.authority } ?: "NONE"

        // Agar foydalanuvchi hali tizimga kirmagan bo'lsa (Login jarayoni)
        if (username == "anonymousUser" || username == "ANONYMOUS") {
            // Metod parametrlarini tekshiramiz (LoginRequest kabi obyekt bormi?)
            joinPoint.args.forEach { arg ->
                try {
                    // Agar bu LoginRequest bo'lsa, uning ichidan usernameni olamiz
                    val usernameField = arg::class.java.getDeclaredField("username")
                    usernameField.isAccessible = true
                    val value = usernameField.get(arg) as? String
                    if (!value.isNullOrBlank()) {
                        username = value
                    }
                } catch (e: Exception) {
                    // Username maydoni bo'lmasa yoki xato bo'lsa o'tkazib yuboramiz
                }
            }
        }

        // Rolni chiroyli ko'rinishga keltiramiz (ROLE_ qismini olib tashlash mumkin yoki shunday qoldirish)
        if (role.contains("ROLE_ANONYMOUS")) {
            role = "GUEST"
        }

        val log = SystemLog(
            username = username,
            role = role,
            action = action,
            category = category,
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
