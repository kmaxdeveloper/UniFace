package com.uniface.dto.matrix

import com.uniface.data.SolveStatus

data class JobStatus(
    val jobId     : String = "",
    val status    : SolveStatus,
    val semester  : Int = 1,
    val hardScore : Int    = 0,
    val softScore : Int    = 0,
    val message   : String? = null
)