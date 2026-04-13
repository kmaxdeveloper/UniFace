package com.uniface

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

@SpringBootApplication // Butun com.uniface paketini skaner qiladi
@EnableJpaRepositories("com.uniface.repository")
@EntityScan("com.uniface.entity")
class UniFaceApplication

fun main(args: Array<String>) {
    runApplication<UniFaceApplication>(*args)
}