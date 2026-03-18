//package com.uniface.matrix
//
//import com.uniface.matrix.service.MatrixService
//import org.springframework.boot.autoconfigure.SpringBootApplication
//import org.springframework.boot.runApplication
//import org.springframework.context.annotation.Bean
//import org.springframework.boot.CommandLineRunner
//import org.springframework.boot.autoconfigure.domain.EntityScan
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories
//
//@SpringBootApplication(
//    exclude = [org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration::class],
//    scanBasePackages = ["com.uniface"]
//)
////@SpringBootApplication(scanBasePackages = ["com.uniface"]) // MUHIM: Hamma modullarni qamrab oladi // Faqat matrix paketini
//@EntityScan(basePackages = ["com.uniface.data", "com.uniface.entity"]) // Faqat core entitylarni
//@EnableJpaRepositories(basePackages = ["com.uniface.repository"])   // Core'dagi Repositorylarni topadi
//class MatrixTestApp {
//    @Bean
//    fun run(matrixService: MatrixService) = CommandLineRunner {
//        // solve() emas, generateTimetable() ni chaqiramiz ✅
//        val solution = matrixService.generateTimetable()
//        matrixService.printTimetable(solution)
//    }
//}
//
//fun main(args: Array<String>) {
//    runApplication<MatrixTestApp>(*args)
//}