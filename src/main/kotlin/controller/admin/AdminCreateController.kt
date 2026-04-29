package com.uniface.controller.admin

import com.uniface.data.LessonType
import com.uniface.dto.TeacherDto
import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.entity.matrix.Building
import com.uniface.entity.matrix.Faculty
import com.uniface.entity.matrix.Room
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.matrix.FacultyRepository
import com.uniface.service.FaceService
import com.uniface.service.UserService
import com.uniface.service.matrix.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/admin")
class AdminCreateController(
    private val studentService: FaceService,
    private val userService: UserService,
    private val subjectRepo: SubjectRepository,
    private val groupRepo: StudentGroupRepository,
    private val facultyRepository: FacultyRepository,
    private val adminService: AdminService
) {
    @PostMapping("/set-group")
    fun addGroup(@RequestBody group: StudentGroup) = ResponseEntity.ok(groupRepo.save(group))

    @PostMapping("/set-subject")
    fun addSubject(@RequestBody subject: Subject) = ResponseEntity.ok(subjectRepo.save(subject))

    @PostMapping("/set-faculty")
    fun addFaculties(@RequestBody faculty: Faculty) = ResponseEntity.ok(facultyRepository.save(faculty))

    @PostMapping("/student/register")
    fun registerStudent(
        @RequestParam id: String,
        @RequestParam fullName: String,
        @RequestParam groupId: Long,
        @RequestParam image: MultipartFile
    ): ResponseEntity<*> = try {
        ResponseEntity.ok(studentService.registerFace(id, fullName, groupId, image.bytes))
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @PostMapping("/set-teacher")
    fun addTeacher(@RequestBody dto: TeacherDto) = try {
        userService.saveTeacher(dto)
        ResponseEntity.ok("O'qituvchi qo'shildi!")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @PostMapping("/set-depart")
    fun addDepartment(@RequestParam name: String, @RequestParam facultyId: Long) =
        ResponseEntity.ok(adminService.addDepartment(name, facultyId))

    @PostMapping("/set-building")
    fun addBuilding(@RequestBody building: Building) = ResponseEntity.ok(adminService.saveBuilding(building))

    @PostMapping("/set-room")
    fun addRoom(@RequestBody room: Room) = ResponseEntity.ok(adminService.saveRoom(room))

    @PostMapping("/set-lesson")
    fun addLesson(
        @RequestParam subjectId: Long,
        @RequestParam teacherId: Long,
        @RequestParam groupIds: List<Long>,
        @RequestParam roomId: Long,
        @RequestParam timeslotId: Long,
        @RequestParam type: LessonType
    ) = try {
        adminService.createLesson(subjectId, teacherId, groupIds, roomId, timeslotId, type)
        ResponseEntity.ok("Dars jadvalga qo'shildi!")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @PostMapping("/set-topic")
    fun addTopic(
        @RequestParam title: String,
        @RequestParam(required = false) description: String?,
        @RequestParam subjectId: Long
    ) = try {
        ResponseEntity.ok(adminService.saveTopic(title, description, subjectId))
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }
}