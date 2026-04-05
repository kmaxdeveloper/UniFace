package com.uniface.controller

import com.uniface.service.AdminImportService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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

    @GetMapping("/template/{type}")
    fun getTemplate(@PathVariable type: String): ResponseEntity<ByteArray> {
        val result = when (type) {
            // 1. Organization: Sheet 0 -> Faculties, Sheet 1 -> Departments
            "org" -> {
                val headers = mapOf(
                    "Faculties" to listOf("Faculty Name"),
                    "Departments" to listOf("Dept Name", "Faculty Name")
                )
                importService.generateMultiSheetTemplate(headers) to "org_template.xlsx"
            }

            // 2. Infrastructure: Sheet 0 -> Buildings, Sheet 1 -> Rooms
            "infra" -> {
                val headers = mapOf(
                    "Buildings" to listOf("Building Name"),
                    "Rooms" to listOf("Room Name", "Capacity", "Type (LECTURE/PRACTICE)", "Building Name")
                )
                importService.generateMultiSheetTemplate(headers) to "infra_template.xlsx"
            }

            // 3. Subjects: Sening importSubjects() koding bo'yicha
            "sub" -> {
                val headers = mapOf("Subjects" to listOf("Name", "Code", "Lecture Hours", "Practice Hours"))
                importService.generateMultiSheetTemplate(headers) to "sub_template.xlsx"
            }

            // 4. Teachers: Sening importTeachers() koding bo'yicha
            "teach" -> {
                val headers = mapOf("Teachers" to listOf("Full Name", "Username", "Email"))
                importService.generateMultiSheetTemplate(headers) to "teach_template.xlsx"
            }

            // 5. Students: Sening importStudents() koding bo'yicha
            "stud" -> {
                val headers = mapOf("Students" to listOf("Full Name", "Student ID", "Group Name"))
                importService.generateMultiSheetTemplate(headers) to "stud_template.xlsx"
            }

            // 6. Allocations: SubjectAllocation entityingga mos
            "alloc" -> {
                val headers = mapOf("Allocations" to listOf("Teacher Username", "Subject Code", "Group Name", "Patok Name"))
                importService.generateMultiSheetTemplate(headers) to "alloc_template.xlsx"
            }

            else -> throw IllegalArgumentException("Tur topilmadi!")
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${result.second}")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(result.first)
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