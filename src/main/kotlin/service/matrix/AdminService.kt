package com.uniface.service.matrix

import com.uniface.data.LessonType
import com.uniface.entity.Lesson
import com.uniface.entity.matrix.Building
import com.uniface.entity.matrix.Department
import com.uniface.entity.matrix.Room
import com.uniface.repository.GroupRepository
import com.uniface.repository.LessonRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.TeacherRepository
import com.uniface.repository.matrix.BuildingRepository
import com.uniface.repository.matrix.DepartmentRepository
import com.uniface.repository.matrix.FacultyRepository
import com.uniface.repository.matrix.RoomRepository
import com.uniface.repository.matrix.TimeslotRepository
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val departmentRepository: DepartmentRepository,
    private val facultyRepository: FacultyRepository,
    private val roomRepository: RoomRepository,
    private val buildingRepository: BuildingRepository,
    private val subjectRepository: SubjectRepository,
    private val lessonRepository: LessonRepository,
    private val timeslotRepository: TimeslotRepository,
    private val teacherRepository: TeacherRepository,
    private val groupRepository: GroupRepository
) {

    // Yangi kafedra qo'shish
    fun addDepartment(name: String, facultyId: Long): Department {
        val faculty = facultyRepository.findById(facultyId)
            .orElseThrow { RuntimeException("Fakultet topilmadi!") }

        val department = Department(name = name, faculty = faculty)
        return departmentRepository.save(department)
    }

    // 1. Fakultet ID bo'yicha kafedralarni olish
    fun getDepartmentsByFaculty(facultyId: Long): List<Department> {
        return departmentRepository.findAllByFacultyId(facultyId)
    }

    // 2. Barcha kafedralarni olish
    fun getAllDepartments(): List<Department> {
        return departmentRepository.findAll()
    }

    // --- BUILDING (Bino) AMALLARI ---
    fun saveBuilding(building: Building): Building {
        return buildingRepository.save(building)
    }

    fun getAllBuildings(): List<Building> {
        return buildingRepository.findAll()
    }

    // --- ROOM (Xona) AMALLARI ---
    fun saveRoom(room: Room): Room {
        // Agar xona qaysidir binoga tegishli bo'lsa,
        // u bazada borligini tekshirib ketsang ham bo'ladi
        return roomRepository.save(room)
    }

    fun getAllRooms(): List<Room> {
        return roomRepository.findAll()
    }

    // Avval kerakli Repository'larni konstruktorda e'lon qilib ol:
// (subjectRepo, teacherRepo, groupRepo, roomRepo, timeslotRepo)

    fun createLesson(
        subjectId: Long,
        teacherId: Long,
        groupIds: List<Long>, // Endi bu yerda bir nechta guruh bo'lishi mumkin
        roomId: Long,
        timeslotId: Long,
        type: LessonType
    ) {
        // 1. Bazadan barcha asosiy obyektlarni topamiz
        val subject = subjectRepository.findById(subjectId)
            .orElseThrow { Exception("Fan topilmadi (ID: $subjectId)") }

        val teacher = teacherRepository.findById(teacherId)
            .orElseThrow { Exception("O'qituvchi topilmadi (ID: $teacherId)") }

        val room = roomRepository.findById(roomId)
            .orElseThrow { Exception("Xona topilmadi (ID: $roomId)") }

        val timeslot = timeslotRepository.findById(timeslotId)
            .orElseThrow { Exception("Vaqt (TimeSlot) topilmadi (ID: $timeslotId)") }

        // 2. Patok darslari uchun hamma guruhlarni birdaniga yig'ib olamiz
        val selectedGroups = groupRepository.findAllById(groupIds)

        if (selectedGroups.isEmpty()) {
            throw Exception("Hech bo'lmaganda bitta guruh tanlanishi shart!")
        }

        // 3. Yangi Lesson (Matrix) obyektini yaratamiz
        val lesson = Lesson(
            subject = subject,
            teacher = teacher,
            groups = selectedGroups.toMutableSet(), // Listni MutableSet'ga o'girib yuboramiz ✅
            room = room,
            timeslot = timeslot,
            type = type
        )

        // 4. Bazaga saqlaymiz
        lessonRepository.save(lesson)
    }
}