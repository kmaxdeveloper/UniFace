package com.uniface

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@EnableJpaRepositories("com.uniface.repository")
class UniFaceApplication

fun main(args: Array<String>) {
    runApplication<UniFaceApplication>(*args)
}