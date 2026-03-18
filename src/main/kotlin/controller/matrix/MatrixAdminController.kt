package com.uniface.controller.matrix

import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.entity.matrix.Department
import com.uniface.entity.matrix.Faculty
import com.uniface.repository.*
import com.uniface.repository.matrix.DepartmentRepository
import com.uniface.repository.matrix.FacultyRepository
import com.uniface.service.matrix.RoomEntryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/matrix/admin")
class MatrixAdminController(
    private val roomService: RoomEntryService,
    private val facultyRepo: FacultyRepository,
    private val deptRepo: DepartmentRepository,
    private val groupRepo: StudentGroupRepository,
    private val subjectRepo: SubjectRepository
) {

    // 1. Xona qo'shish (Eski mantiq saqlandi)
    @PostMapping("/add-room")
    fun addRoom(
        @RequestParam bino: String,
        @RequestParam xona: String,
        @RequestParam sigim: Int,
        @RequestParam lab: Boolean
    ): ResponseEntity<String> {
        roomService.saveRoom(bino, xona, sigim, lab)
        return ResponseEntity.ok("Xona muvaffaqiyatli saqlandi! ✅")
    }

    // 2. Fakultet qo'shish
    @PostMapping("/add-faculty")
    fun addFaculty(@RequestParam name: String): ResponseEntity<Faculty> {
        val faculty = facultyRepo.save(Faculty(name = name))
        return ResponseEntity.ok(faculty)
    }

    // 3. Kafedra qo'shish (Fakultetga bog'langan holda)
    @PostMapping("/add-department")
    fun addDepartment(
        @RequestParam name: String,
        @RequestParam facultyId: Long
    ): ResponseEntity<String> {
        val faculty = facultyRepo.findById(facultyId).orElseThrow { Exception("Fakultet topilmadi!") }
        deptRepo.save(Department(name = name, faculty = faculty))
        return ResponseEntity.ok("$name kafedrasi ${faculty.name}ga biriktirildi! ✅")
    }

    // 4. Guruh qo'shish
    @PostMapping("/add-group")
    fun addGroup(
        @RequestParam name: String,
        @RequestParam count: Int,
        @RequestParam facultyId: Long
    ): ResponseEntity<StudentGroup> {
        val faculty = facultyRepo.findById(facultyId).orElseThrow { Exception("Fakultet topilmadi!") }
        val group = groupRepo.save(StudentGroup(name = name, studentCount = count, faculty = faculty))
        return ResponseEntity.ok(group)
    }

    // 5. Fan qo'shish (Kafedraga bog'langan holda)
    @PostMapping("/add-subject")
    fun addSubject(
        @RequestParam name: String,
        @RequestParam code: String,
        @RequestParam lecture: Int,
        @RequestParam lab: Int,
        @RequestParam deptId: Long
    ): ResponseEntity<Subject> {
        val dept = deptRepo.findById(deptId).orElseThrow { Exception("Kafedra topilmadi!") }
        val subject = subjectRepo.save(Subject(
            name = name,
            code = code,
            lectureHours = lecture,
            labHours = lab,
            department = dept // Entityda shu field bo'lishi kerak
        ))
        return ResponseEntity.ok(subject)
    }
}