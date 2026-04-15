package com.uniface.config

import ai.timefold.solver.core.api.solver.SolverManager
import ai.timefold.solver.core.config.solver.SolverConfig
import ai.timefold.solver.core.config.solver.SolverManagerConfig
import ai.timefold.solver.core.config.solver.termination.TerminationConfig
import com.uniface.entity.Lesson
import com.uniface.matrix.solver.TimetableConstraintProvider
import com.uniface.matrix.solver.TimetableSolution
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Timefold Spring Bean konfiguratsiyasi.
 *
 * SolverManager → async solve uchun ishlatiladi
 * SolverConfig  → qanday ishlashi (qoidalar, limit)
 */

/*
   ~~~ Ishlamasa buni o'chrib qo'yish kerak !
 */
@Configuration
class TimefoldConfig {

    @Bean
    fun solverConfig(): SolverConfig =
        SolverConfig()
            .withSolutionClass(TimetableSolution::class.java)
            .withEntityClasses(Lesson::class.java)
            .withConstraintProviderClass(TimetableConstraintProvider::class.java)
            .withTerminationConfig(
                TerminationConfig()
                    .withSpentLimit(Duration.ofSeconds(30))  // max 30 soniya
                    .withBestScoreFeasible(true)             // hard=0 bo'lsa to'xtaydi
            )

    @Bean
    fun solverManager(solverConfig: SolverConfig): SolverManager<TimetableSolution, String> =
        SolverManager.create(
            solverConfig,
            SolverManagerConfig().withParallelSolverCount("AUTO")
        )
}