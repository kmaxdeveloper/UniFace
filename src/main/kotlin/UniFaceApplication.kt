package com.uniface

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class UniFaceApplication

fun main(args: Array<String>) {
    runApplication<UniFaceApplication>(*args)
}