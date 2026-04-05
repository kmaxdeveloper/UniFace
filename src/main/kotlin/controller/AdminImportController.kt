package com.uniface.controller

import com.uniface.service.AdminImportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/admin/import")
class AdminImportController(private val importService: AdminImportService) {

    // Tashkiliy tuzilma (Fakultet va Kafedra)
    @PostMapping("/org")
    fun importOrg(@RequestParam file: MultipartFile) = handle(file) {
        importService.importOrgStructure(file)
    }

    // Bino va Xonalar
    @PostMapping("/infra")
    fun importInfra(@RequestParam file: MultipartFile) = handle(file) {
        importService.importInfrastructure(file)
    }

    // Fanlar
    @PostMapping("/sub")
    fun importSub(@RequestParam file: MultipartFile, @RequestParam deptId: Long) = handle(file) {
        importService.importSubjects(file, deptId)
    }

    // O'qituvchilar
    @PostMapping("/teach")
    fun importTeach(
        @RequestParam file: MultipartFile,
        @RequestParam faculty: String,
        @RequestParam dept: String
    ) = handle(file) { importService.importTeachers(file, faculty, dept) }

    // Talabalar
    @PostMapping("/stud")
    fun importStud(@RequestParam file: MultipartFile, @RequestParam facultyId: Long) = handle(file) {
        importService.importStudents(file, facultyId)
    }

    // Kim-kimga dars o'tishi (Reja)
    @PostMapping("/alloc")
    fun importAlloc(@RequestParam file: MultipartFile) = handle(file) {
        importService.importAllocations(file)
    }

    private fun handle(file: MultipartFile, action: () -> Unit): ResponseEntity<Map<String, String>> {
        return try {
            // 1. Birinchi navbatda faylni tekshiramiz
            validate(file)
            // 2. Hammasi OK bo'lsa, servisni ishga tushiramiz
            action()
            ResponseEntity.ok(mapOf("msg" to "OK"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("err" to (e.message ?: "Error")))
        }
    }

    private fun validate(file: MultipartFile) {
        val name = file.originalFilename?.lowercase() ?: ""
        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            throw IllegalArgumentException("Faqat Excel (.xlsx, .xls) yuklang!")
        }
    }
}