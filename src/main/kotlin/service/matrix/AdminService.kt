package com.uniface.service.matrix

import com.uniface.data.LessonType
import com.uniface.entity.Lesson
import com.uniface.entity.Student
import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.entity.matrix.Building
import com.uniface.entity.matrix.Department
import com.uniface.entity.matrix.Room
import com.uniface.repository.AttendanceRepository
import com.uniface.repository.GroupRepository
import com.uniface.repository.LessonRepository
import com.uniface.repository.StudentRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.TeacherRepository
import com.uniface.repository.matrix.BuildingRepository
import com.uniface.repository.matrix.DepartmentRepository
import com.uniface.repository.matrix.FacultyRepository
import com.uniface.repository.matrix.RoomRepository
import com.uniface.repository.matrix.TimeslotRepository
import com.uniface.repository.TopicRepository
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
    private val groupRepository: GroupRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val topicRepository: TopicRepository
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

    // --- XONANI YANGILASH ---
    fun updateRoom(id: Long, roomDetails: Room): Room {
        // 1. Bazadan eski xonani qidiramiz
        val existingRoom = roomRepository.findById(id)
            .orElseThrow { RuntimeException("Xona topilmadi! (ID: $id)") }

        // 2. Ma'lumotlarni yangilaymiz
        existingRoom.roomNumber = roomDetails.roomNumber
        existingRoom.capacity = roomDetails.capacity
        existingRoom.isLaboratory = roomDetails.isLaboratory

        // 3. Agar bino (building) ma'lumoti kelsa, uni ham yangilaymiz
        if (roomDetails.building != null) {
            existingRoom.building = roomDetails.building
        }

        // 4. Saqlaymiz
        return roomRepository.save(existingRoom)
    }

    // --- BINO (Building) YANGILASH ---
    fun updateBuilding(id: Long, details: Building): Building {
        val building = buildingRepository.findById(id)
            .orElseThrow { RuntimeException("Bino topilmadi! (ID: $id)") }

        building.name = details.name
        // Agar boshqa maydonlar (masalan: address) bo'lsa, ularni ham shu yerda update qilasan
        return buildingRepository.save(building)
    }

    // --- KAFEDRA (Department) YANGILASH ---
    fun updateDepartment(id: Long, details: Department): Department {
        val department = departmentRepository.findById(id)
            .orElseThrow { RuntimeException("Kafedra topilmadi! (ID: $id)") }

        department.name = details.name

        // Agar kafedraning fakulteti o'zgarsa
        if (details.faculty != null) {
            val faculty = facultyRepository.findById(details.faculty!!.id)
                .orElseThrow { RuntimeException("Yangi fakultet topilmadi!") }
            department.faculty = faculty
        }

        return departmentRepository.save(department)
    }

    // --- TALABA (Student) YANGILASH ---
    // ID bu yerda String formatda (masalan: "U12345")
    fun updateStudent(id: String, fullName: String, groupId: Long): Student {
        val student = studentRepository.findById(id)
            .orElseThrow { RuntimeException("Talaba topilmadi! (ID: $id)") }

        val group = groupRepository.findById(groupId)
            .orElseThrow { RuntimeException("Guruh topilmadi! (ID: $groupId)") }

        student.fullName = fullName
        student.group = group

        return studentRepository.save(student)
    }

    // =================== DELETE =============================
    // --- O'CHIRISH AMALLARI ---

    fun deleteBuilding(id: Long) {
        if (!buildingRepository.existsById(id)) throw Exception("Bino topilmadi!")
        buildingRepository.deleteById(id)
    }

    fun deleteRoom(id: Long) {
        if (!roomRepository.existsById(id)) throw Exception("Xona topilmadi!")
        roomRepository.deleteById(id)
    }

    fun deleteDepartment(id: Long) {
        if (!departmentRepository.existsById(id)) throw Exception("Kafedra topilmadi!")
        departmentRepository.deleteById(id)
    }

    fun deleteFaculty(id: Long) {
        if (!facultyRepository.existsById(id)) throw Exception("Fakultet topilmadi!")
        facultyRepository.deleteById(id)
    }

    fun deleteStudent(id: String) { // ID String ekanligini hisobga oldik
        if (!studentRepository.existsById(id)) throw Exception("Talaba topilmadi!")
        // DIQQAT: AWS Rekognition'dan ham o'chirish mantiqini keyinchalik FaceService'ga qo'shish kerak
        studentRepository.deleteById(id)
    }

    fun deleteTeacher(id: Long) {
        // Teacher o'chirilganda uning User akkaunti ham o'chirilishi kerak bo'lsa,
        // buni userService orqali qilish ma'qulroq
        teacherRepository.deleteById(id)
    }

    fun deleteGroup(id: Long) {
        if (!groupRepository.existsById(id)) throw Exception("Guruh topilmadi!")
        groupRepository.deleteById(id)
    }

    fun deleteSubject(id: Long) {
        if (!subjectRepository.existsById(id)) throw Exception("Fan topilmadi!")
        subjectRepository.deleteById(id)
    }

    // =============================================================================
    // --- QUERY LOGIC ---

    // Fakultetga tegishli guruhlarni olish
    fun getGroupsByFaculty(facultyId: Long): List<StudentGroup> {
        return groupRepository.findAllByFacultyId(facultyId)
    }

    // Talabani ismi bo'yicha qidirish (Like query)
    fun searchStudentsByName(name: String): List<Student> {
        return studentRepository.findAllByFullNameContainingIgnoreCase(name)
    }

    // Binoga tegishli xonalarni olish
    fun getRoomsByBuilding(buildingId: Long): List<Room> {
        return roomRepository.findAllByBuildingId(buildingId)
    }

    // Kafedraga tegishli fanlarni olish (SubjectRepository'da kafedra bog'liqligi bo'lsa)
    fun getSubjectsByDepartment(deptId: Long): List<Subject> {
        return subjectRepository.findAllByDepartmentId(deptId)
    }

    // 1. GURUHNING DARS JADVALINI OLISH
    fun getScheduleByGroup(groupId: Long): List<Lesson> {
        // Lesson ichidagi 'groups' bu Set/List bo'lgani uchun repositoryda maxsus query kerak
        return lessonRepository.findAllByGroupsId(groupId)
    }

    // 2. BUGUNGI UMUMIY DAVOMAT STATISTIKASI (Admin Dashboard uchun)
    fun getTodayAttendanceStats(): Map<String, Any> {
        val totalStudents = studentRepository.count()

        // Bugun 00:00 dan 23:59 gacha bo'lgan vaqt oralig'i
        val startOfDay = java.time.LocalDate.now().atStartOfDay()
        val endOfDay = java.time.LocalDate.now().atTime(23, 59, 59)

        val presentCount = attendanceRepository.countPresentToday(startOfDay, endOfDay)

        return mapOf(
            "total" to totalStudents,
            "present" to presentCount,
            "absent" to (totalStudents - presentCount),
            "percentage" to if (totalStudents > 0L) (presentCount * 100 / totalStudents) else 0
        )
    }

    // 3. ENG KO'P DARS QOLDIRGAN TALABALAR (Qora ro'yxat)
    fun getTopAbsentStudents(limit: Int): List<Any> {
        // Bu yerda murakkabroq Query kerak: Har bir talaba uchun ABSENTlarni sanab, sort qilamiz
        // Hozircha sodda ko'rinishda (Query orqali chaqiriladigan metod)
        return attendanceRepository.findTopAbsentStudents(limit)
    }

    // 4. XONANING BANDLIGINI TEKSHIRISH (Matrix Conflict)
    fun isRoomAvailable(roomId: Long, timeslotId: Long): Boolean {
        // Shu xonada va shu vaqtda dars bormi?
        val exists = lessonRepository.existsByRoomIdAndTimeslotId(roomId, timeslotId)
        return !exists // Agar dars bo'lmasa true (ya'ni bo'sh) qaytaradi
    }

    // =================== TOPIC (Mavzu) CRUD =====================

    fun saveTopic(title: String, description: String?, subjectId: Long): com.uniface.entity.Topic {
        val subject = subjectRepository.findById(subjectId)
            .orElseThrow { Exception("Fan topilmadi (ID: $subjectId)") }
        val topic = com.uniface.entity.Topic(title, subject, description)
        return topicRepository.save(topic)
    }

    fun updateTopic(id: Long, title: String, description: String?): com.uniface.entity.Topic {
        val topic = topicRepository.findById(id)
            .orElseThrow { Exception("Mavzu topilmadi (ID: $id)") }
        topic.title = title
        topic.description = description
        return topicRepository.save(topic)
    }

    fun deleteTopic(id: Long) {
        if (!topicRepository.existsById(id)) throw Exception("Mavzu topilmadi!")
        topicRepository.deleteById(id)
    }

    fun getTopicsBySubject(subjectId: Long): List<com.uniface.entity.Topic> {
        return topicRepository.findBySubjectId(subjectId)
    }
}