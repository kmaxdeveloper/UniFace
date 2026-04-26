package com.uniface.config

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Loggable(
    val action: String = "",
    val category: String = "SYSTEM"
)
