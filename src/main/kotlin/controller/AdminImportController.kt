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
                val sheets = mapOf(
                    "Faculties" to listOf("Faculty Name"),
                    "Departments" to listOf("Dept Name", "Faculty Name")
                )
                val samples = mapOf(
                    "Faculties" to listOf(listOf("Kompyuter Injiniringi")),
                    "Departments" to listOf(listOf("Dasturiy injiniring", "Kompyuter Injiniringi"))
                )
                importService.generateSmartTemplate(sheets, samples) to "org_template.xlsx"
            }

            // 2. Infrastructure: Sheet 0 -> Buildings, Sheet 1 -> Rooms
            "infra" -> {
                val sheets = mapOf(
                    "Buildings" to listOf("Building Name", "Floors"),
                    "Rooms" to listOf("Room Number", "Capacity", "Is Laboratory (TRUE/FALSE)", "Building Name")
                )
                val samples = mapOf(
                    "Buildings" to listOf(listOf("A-Bino", "4")),
                    "Rooms" to listOf(listOf("101", "30", "FALSE", "A-Bino"), listOf("202", "15", "TRUE", "A-Bino"))
                )
                importService.generateSmartTemplate(sheets, samples) to "infra_template.xlsx"
            }

            // 3. Subjects: Name, Code, Lecture, Lab (Kodingdagi importSubjects ga mos)
            "sub" -> {
                val sheets = mapOf("Subjects" to listOf("Code", "Name", "Lecture Hours", "Laboratory Hours"))
                val samples = mapOf("Subjects" to listOf(listOf("CSE101", "Ma'lumotlar Strukturasi", "36", "18")))
                importService.generateSmartTemplate(sheets, samples) to "sub_template.xlsx"
            }

            // 4. Teachers: Full Name, Username, Subjects (split by comma)
            "teach" -> {
                val sheets = mapOf("Teachers" to listOf("Full Name", "Username", "Subject Codes (Comma separated)"))
                val samples = mapOf("Teachers" to listOf(listOf("Eshmatov Toshmat", "toshmat_e", "CSE101, MAT202")))
                importService.generateSmartTemplate(sheets, samples) to "teachers_template.xlsx"
            }

            // 5. Students: Student ID, Full Name, Face ID, Group Name
            "stud" -> {
                val sheets = mapOf("Students" to listOf("Student ID", "Full Name", "Face ID", "Group Name"))
                val samples = mapOf("Students" to listOf(listOf("210-22", "Ali Valiyev", "FACE_21022", "611-21")))
                importService.generateSmartTemplate(sheets, samples) to "students_template.xlsx"
            }

            // 6. Allocations: Teacher Username, Subject Code, Group, Patok
            "alloc" -> {
                val sheets = mapOf("Allocations" to listOf("Teacher Username", "Subject Code", "Group Name", "Patok Name"))
                val samples = mapOf("Allocations" to listOf(listOf("toshmat_e", "CSE101", "611-21", "")))
                importService.generateSmartTemplate(sheets, samples) to "alloc_template.xlsx"
            }

            else -> throw IllegalArgumentException("Tur topilmadi!")
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${result.second}\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
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