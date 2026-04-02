package com.uniface.controller

import com.uniface.dto.TeacherDto
import com.uniface.dto.UserDto
import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.entity.matrix.Department
import com.uniface.entity.matrix.Faculty
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.matrix.DepartmentRepository
import com.uniface.repository.matrix.FacultyRepository
import com.uniface.service.FaceService
import com.uniface.service.UserService
import com.uniface.service.matrix.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val faceService: FaceService,
    private val userService: UserService, // ✅ UserService qo'shildi
    private val subjectRepo: SubjectRepository,
    private val groupRepo: StudentGroupRepository,
    private val facultyRepository: FacultyRepository,
    private val adminService: AdminService,
    private val departmentRepository: DepartmentRepository
) {

    //=====================================================================================
    // --- 1. GURUHLARNI BOSHQARISH ---
    @PostMapping("/add-groups")
    fun addGroup(@RequestBody group: StudentGroup) = ResponseEntity.ok(groupRepo.save(group))

    @GetMapping("/get-groups")
    fun getGroups() = ResponseEntity.ok(groupRepo.findAll())
    //=====================================================================================

    //=====================================================================================
    // --- 2. FANLARNI BOSHQARISH ---
    @PostMapping("/set-subjects")
    fun addSubject(@RequestBody subject: Subject) = ResponseEntity.ok(subjectRepo.save(subject))

    @GetMapping("/get-subjects")
    fun getSubjects() = ResponseEntity.ok(subjectRepo.findAll())
    //======================================================================================

    // =====================================================================================
    @GetMapping("/get-faculties")
    fun getFaculties() = ResponseEntity.ok(facultyRepository.findAll())

    @PostMapping("/set-faculties")
    fun addFaculties(@RequestBody faculty: Faculty) = ResponseEntity.ok(facultyRepository.save(faculty))
    //======================================================================================

    //======================================================================================
    // --- 3. TALABANI RO'YXATDAN O'TKAZISH (AWS + DB) ---
    @PostMapping("/student/register")
    fun registerStudent(
        @RequestParam("id") id: String,
        @RequestParam("fullName") fullName: String,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("image") file: MultipartFile
    ): ResponseEntity<*> {
        return try {
            val response = faceService.registerFace(id, fullName, groupId, file.bytes)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
    //======================================================================================

    // --- 4. USTOZLARNI BOSHQARISH ---
    @PostMapping("/add-teacher")
    fun addTeacher(@RequestBody dto: TeacherDto): ResponseEntity<String> {
        return try {
            userService.saveTeacher(dto)
            ResponseEntity.ok("O'qituvchi muvaffaqiyatli qo'shildi!")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Xatolik: ${e.message}")
        }
    }

    @PutMapping("/update-teacher/{id}")
    fun updateTeacher(@PathVariable id: Long, @RequestBody request: UserDto): ResponseEntity<String> {
        return try {
            userService.updateUser(id, request)
            ResponseEntity.ok("Ma'lumotlar yangilandi!")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PostMapping("/set-departments")
    fun addDepartment(
        @RequestParam name: String,
        @RequestParam facultyId: Long
    ): ResponseEntity<Department> {
        val savedDepartment = adminService.addDepartment(name, facultyId)
        return ResponseEntity.ok(savedDepartment)
    }

    @GetMapping("/get-departments")
    fun getDepartments(@RequestParam(required = false) facultyId: Long?): ResponseEntity<List<Department>> {
        val list = if (facultyId != null) {
            adminService.getDepartmentsByFaculty(facultyId)
        } else {
            adminService.getAllDepartments()
        }
        return ResponseEntity.ok(list)
    }
}