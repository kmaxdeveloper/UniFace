package com.uniface.controller.admin

import com.uniface.service.matrix.ImportService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/admin/import")
class AdminImportController(private val importService: ImportService) {

    // 1. Fakultetlar (Alohida)
    @PostMapping("/faculties")
    fun importFaculties(@RequestParam file: MultipartFile) = handle(file) {
        importService.importFaculties(file)
    }

    // 2. Kafedralar (Alohida)
    @PostMapping("/departments")
    fun importDepartments(@RequestParam file: MultipartFile) = handle(file) {
        importService.importDepartments(file)
    }

    // 3. Binolar
    @PostMapping("/buildings")
    fun importBuildings(@RequestParam file: MultipartFile) = handle(file) {
        importService.importBuildings(file)
    }

    // 4. Xonalar (Building + Room logic)
    @PostMapping("/rooms")
    fun importRooms(@RequestParam file: MultipartFile) = handle(file) {
        importService.importRooms(file)
    }

    // 5. Fanlar
    @PostMapping("/subjects")
    fun importSub(@RequestParam file: MultipartFile) = handle(file) {
        importService.importSubjects(file)
    }

    // 6. O'qituvchilar
    @PostMapping("/teachers")
    fun importTeach(@RequestParam file: MultipartFile) = handle(file) {
        importService.importTeachers(file)
    }

    // 7. Talabalar
    @PostMapping("/students")
    fun importStud(@RequestParam file: MultipartFile) = handle(file) {
        importService.importStudents(file)
    }

    // 8. Dars jadvali (TimeSlot, Potok va Room logic bilan)
    @PostMapping("/lessons")
    fun importLessons(@RequestParam file: MultipartFile) = handle(file) {
        importService.importLessons(file)
    }

    // 9. Reja / Allocations
    @PostMapping("/allocations")
    fun importAlloc(@RequestParam file: MultipartFile) = handle(file) {
        importService.importAllocations(file)
    }

    @PostMapping("/import-excel")
    fun importExcel(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        return try {
            importService.importCurriculum(file)
            ResponseEntity.ok("Excel muvaffaqiyatli yuklandi va sillabus shakllantirildi!")
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("Xatolik: ${e.message}")
        }
    }

    // --- TEMPLATES ---
    @GetMapping("/template/{type}")
    fun getTemplate(@PathVariable type: String): ResponseEntity<ByteArray> {
        val result = when (type) {
            "faculties" -> mapOf("Faculties" to listOf("Faculty Name")) to mapOf("Faculties" to listOf(listOf("KIF")))
            "departments" -> mapOf("Departments" to listOf("Dept Name", "Faculty Name")) to mapOf("Departments" to listOf(listOf("Dasturiy injiniring", "KIF")))
            "buildings" -> mapOf("Buildings" to listOf("Building Name", "Floors")) to mapOf("Buildings" to listOf(listOf("A-Bino", "4")))
            "rooms" -> mapOf("Rooms" to listOf("Room Number", "Capacity", "Type (LAB/LEC)", "Building Name")) to mapOf("Rooms" to listOf(listOf("101", "30", "LEC", "A-Bino")))
            "subjects" -> mapOf("Subjects" to listOf("Code", "Name", "Lecture Hours", "Lab Hours", "Dept Name")) to mapOf("Subjects" to listOf(listOf("CSE101", "Algorithm", "36", "18", "Dasturiy injiniring")))
            "teachers" -> mapOf("Teachers" to listOf("Full Name", "Username", "Email", "Dept", "Faculty")) to mapOf("Teachers" to listOf(listOf("Ali Valiyev", "ali_v", "ali@tatu.uz", "DI", "KIF")))
            "students" -> mapOf("Students" to listOf("Student ID", "Full Name", "Face ID", "Group Name")) to mapOf("Students" to listOf(listOf("210-22", "Vali Aliyev", "FACE_22", "611-21")))
            "lessons" -> mapOf("Lessons" to listOf("Day(1-6)", "Slot(1-7)", "SubCode", "Room", "Building", "Groups", "TeacherUser", "Type")) to mapOf("Lessons" to listOf(listOf("1", "1", "CSE101", "101", "A-Bino", "611-21, 612-21", "ali_v", "LECTURE")))
            "allocations" -> mapOf("Allocations" to listOf("Teacher Username", "Subject Code", "Group Name")) to mapOf("Allocations" to listOf(listOf("ali_v", "CSE101", "611-21")))
            "curriculums" -> mapOf("Curriculums" to listOf("Group Name", "Subject Name", "Hours Per Week", "Semester")) to
                    mapOf("Curriculums" to listOf(listOf("941-21", "Backend (Kotlin)", "4", "4")))
            else -> throw IllegalArgumentException("Tur topilmadi!")
        }

        // Eslab qol error berishi mumkin !
        val template = importService.generateSmartTemplate(result.first, result.second)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${type}_template.xlsx\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(template)
    }

    // --- HELPERS ---
    private fun handle(file: MultipartFile, action: () -> String): ResponseEntity<Map<String, String>> {
        return try {
            validate(file)
            val message = action()
            ResponseEntity.ok(mapOf("msg" to message))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("err" to (e.message ?: "Noma'lum xatolik")))
        }
    }

    private fun validate(file: MultipartFile) {
        val name = file.originalFilename?.lowercase() ?: ""
        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            throw IllegalArgumentException("Faqat Excel (.xlsx, .xls) yuklang!")
        }
    }
}