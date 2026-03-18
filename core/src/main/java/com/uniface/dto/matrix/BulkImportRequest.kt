package com.uniface.dto.matrix

data class BulkImportRequest(
    val buildings: List<com.uniface.dto.matrix.BuildingImportDTO>,
    val subjects: List<com.uniface.dto.matrix.SubjectImportDTO>,
    val groups: List<com.uniface.dto.matrix.GroupImportDTO>
)