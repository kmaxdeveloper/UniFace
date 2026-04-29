package com.uniface.controller.admin

import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.matrix.FacultyRepository
import com.uniface.service.FaceService
import com.uniface.service.UserService
import com.uniface.service.matrix.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class AdminReadController(
    private val studentService: FaceService,
    private val userService: UserService,
    private val subjectRepo: SubjectRepository,
    private val groupRepo: StudentGroupRepository,
    private val facultyRepository: FacultyRepository,
    private val adminService: AdminService
) {
    @GetMapping("/get-groups")
    fun getGroups() = ResponseEntity.ok(groupRepo.findAll())

    @GetMapping("/get-subjects")
    fun getSubjects() = ResponseEntity.ok(subjectRepo.findAll())

    @GetMapping("/get-faculties")
    fun getFaculties() = ResponseEntity.ok(facultyRepository.findAll())

    @GetMapping("/get-students")
    fun getStudents(@RequestParam(required = false) groupId: Long?) =
        ResponseEntity.ok(studentService.getStudents(groupId))

    @GetMapping("/get-teachers")
    fun getTeachers() = ResponseEntity.ok(userService.getAllTeachers())

    @GetMapping("/get-depart")
    fun getDepartments(@RequestParam(required = false) facultyId: Long?) =
        ResponseEntity.ok(if (facultyId != null) adminService.getDepartmentsByFaculty(facultyId) else adminService.getAllDepartments())

    @GetMapping("/get-buildings")
    fun getBuildings() = ResponseEntity.ok(adminService.getAllBuildings())

    @GetMapping("/get-rooms")
    fun getRooms() = ResponseEntity.ok(adminService.getAllRooms())

    // =============== Search By Query ===========================
    // 1. Guruhlar (Fakultet bo'yicha)
    @GetMapping("/get-groups/faculty/{facultyId}")
    fun groups(@PathVariable facultyId: Long) = ResponseEntity.ok(adminService.getGroupsByFaculty(facultyId))

    // 2. Talabalar (Ism bo'yicha qidiruv)
    @GetMapping("/get-students/search")
    fun search(@RequestParam name: String) = ResponseEntity.ok(adminService.searchStudentsByName(name))

    // 3. Fanlar (Kafedra bo'yicha)
    @GetMapping("/get-subjects/dept/{deptId}")
    fun subjects(@PathVariable deptId: Long) = ResponseEntity.ok(adminService.getSubjectsByDepartment(deptId))

    // 4. Xonalar (Bino bo'yicha)
    @GetMapping("/get-rooms/building/{buildingId}")
    fun rooms(@PathVariable buildingId: Long) = ResponseEntity.ok(adminService.getRoomsByBuilding(buildingId))

    // ===================== Query ========================================
    // Bugungi umumiy davomat foizi va soni
    @GetMapping("/get-stats/attendance/today")
    fun todayAttendance() = ResponseEntity.ok(adminService.getTodayAttendanceStats())

    // Guruh ID bo'yicha dars jadvalini olish
    @GetMapping("/get-schedule/group/{groupId}")
    fun groupSchedule(@PathVariable groupId: Long) = ResponseEntity.ok(adminService.getScheduleByGroup(groupId))

    // Eng ko'p dars qoldirgan top 10 talaba
    @GetMapping("/get-stats/absent-students")
    fun topAbsentStudents(@RequestParam(defaultValue = "10") limit: Int) =
        ResponseEntity.ok(adminService.getTopAbsentStudents(limit))

    // Xona va vaqt bo'yicha bandlikni tekshirish
    @GetMapping("/get-rooms/check-availability")
    fun checkRoom(@RequestParam roomId: Long, @RequestParam timeslotId: Long) =
        ResponseEntity.ok(adminService.isRoomAvailable(roomId, timeslotId))

    @GetMapping("/get-topics/subject/{subjectId}")
    fun getTopics(@PathVariable subjectId: Long) = ResponseEntity.ok(adminService.getTopicsBySubject(subjectId))
}