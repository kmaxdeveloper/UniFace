package com.uniface.config

import ai.timefold.solver.core.config.solver.SolverConfig
import com.uniface.matrix.domain.Timetable
import com.uniface.entity.Lesson
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@Configuration
class TimefoldConfig3 {

//    @Bean
//    fun solverConfig(): SolverConfig {
//        return SolverConfig()
//            .withSolutionClass(Timetable::class.java)
//            .withEntityClasses(Lesson::class.java)
//            // Constraint provider klassingni ham ko'rsatib ketishing kerak bo'ladi
//            .withConstraintProviderClass(com.uniface.matrix.solver.MatrixConstraintProvider::class.java)
//            .withTerminationConfig(ai.timefold.solver.core.config.solver.termination.TerminationConfig()
//                .withSpentLimit(java.time.Duration.ofSeconds(30))) // 30 soniya hisoblaydi
//    }
}