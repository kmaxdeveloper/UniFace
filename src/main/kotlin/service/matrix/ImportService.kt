package com.uniface.service.matrix

import com.uniface.data.LessonType
import com.uniface.data.Role
import com.uniface.dto.matrix.UniversityDataImport
import com.uniface.entity.Lesson
import com.uniface.entity.Student
import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.entity.SubjectAllocation
import com.uniface.entity.Teacher
import com.uniface.entity.User
import com.uniface.entity.matrix.Building
import com.uniface.entity.matrix.Department
import com.uniface.entity.matrix.Faculty
import com.uniface.entity.matrix.Room
import com.uniface.repository.GroupRepository
import com.uniface.repository.LessonRepository
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.StudentRepository
import com.uniface.repository.SubjectAllocationRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.TeacherRepository
import com.uniface.repository.matrix.BuildingRepository
import com.uniface.repository.matrix.DepartmentRepository
import com.uniface.repository.matrix.FacultyRepository
import com.uniface.repository.matrix.RoomRepository
import com.uniface.repository.matrix.TimeslotRepository
import jakarta.transaction.Transactional
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImportService(
    private val roomService: RoomEntryService,
    private val departmentRepository: DepartmentRepository,
    private val facultyRepository: FacultyRepository,
    private val roomRepository: RoomRepository,
    private val buildingRepository: BuildingRepository,
    private val subjectRepository: SubjectRepository,
    private val passwordEncoder: PasswordEncoder,
    private val teacherRepository: TeacherRepository,
    private val groupRepository: GroupRepository,
    private val studentRepository: StudentRepository,
    private val subjectAllocationRepository: SubjectAllocationRepository,
    private val lessonRepository: LessonRepository,
    private val timeslotRepository: TimeslotRepository
) {

    // --- 1. FACULTIES ---
    @Transactional
    fun importFaculties(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val name = row.getCell(0)?.toString()?.trim() ?: continue

            if (facultyRepository.findByName(name) == null) {
                facultyRepository.save(Faculty(name = name))
                count++
            }
        }
        workbook.close()
        return "$count ta fakultet qo'shildi"
    }

    // --- 2. DEPARTMENTS ---
    @Transactional
    fun importDepartments(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val deptName = row.getCell(0)?.toString()?.trim() ?: continue
            val facName = row.getCell(1)?.toString()?.trim() ?: continue

            val faculty = facultyRepository.findByName(facName)
                ?: throw RuntimeException("$facName fakulteti topilmadi! Oldin fakultetni yuklang.")

            if (departmentRepository.findByName(deptName) == null) {
                departmentRepository.save(Department(name = deptName, faculty = faculty))
                count++
            }
        }
        workbook.close()
        return "$count ta kafedra qo'shildi"
    }

    // --- 3. BUILDINGS ---
    @Transactional
    fun importBuildings(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val name = row.getCell(0)?.toString()?.trim() ?: continue
            val floors = row.getCell(1)?.numericCellValue?.toInt() ?: 1

            if (buildingRepository.findByName(name) == null) {
                buildingRepository.save(Building(name = name, floorCount = floors))
                count++
            }
        }
        workbook.close()
        return "$count ta bino qo'shildi"
    }

    // --- 4. ROOMS ---
    @Transactional
    fun importRooms(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val roomNo = row.getCell(0)?.toString()?.trim() ?: continue
            val cap = row.getCell(1)?.numericCellValue?.toInt() ?: 30
            val type = row.getCell(2)?.toString()?.trim()?.uppercase() ?: "LEC"
            val bName = row.getCell(3)?.toString()?.trim() ?: continue

            val building = buildingRepository.findByName(bName) ?: continue

            // Dublikatga tekshirish
            if (roomRepository.findByRoomNumberAndBuilding(roomNo, building) == null) {
                roomRepository.save(Room(
                    roomNumber = roomNo,
                    capacity = cap,
                    isLaboratory = (type == "LAB"),
                    building = building
                ))
                count++
            }
        }
        workbook.close()
        return "$count ta xona qo'shildi"
    }

    // --- 5. SUBJECTS ---
    @Transactional
    fun importSubjects(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val code = row.getCell(0)?.toString()?.trim() ?: continue
            val name = row.getCell(1)?.toString()?.trim() ?: continue
            val lec = row.getCell(2)?.numericCellValue?.toInt() ?: 0
            val lab = row.getCell(3)?.numericCellValue?.toInt() ?: 0
            val dName = row.getCell(4)?.toString()?.trim() ?: ""

            val dept = departmentRepository.findByName(dName)
            if (subjectRepository.findByCode(code) == null) {
                subjectRepository.save(Subject(name = name, code = code, lectureHours = lec, labHours = lab, department = dept))
                count++
            }
        }
        workbook.close()
        return "$count ta fan qo'shildi"
    }

    // --- 6. TEACHERS ---
    @Transactional
    fun importTeachers(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val fName = row.getCell(0)?.toString()?.trim() ?: continue
            val uName = row.getCell(1)?.toString()?.trim() ?: continue
            val dName = row.getCell(3)?.toString()?.trim() ?: ""
            val facName = row.getCell(4)?.toString()?.trim() ?: ""

            if (teacherRepository.findByUserUsername(uName) == null) {
                val user = User(
                    username = uName,
                    password = passwordEncoder.encode("tatu1234"),
                    fullName = fName,
                    role = Role.ROLE_TEACHER
                )
                val teacher = Teacher(fullName = fName, user = user, department = dName, faculty = facName, status = true)
                user.teacherProfile = teacher
                // User-Teacher mantiqida Cascade bor deb hisoblaymiz
                teacherRepository.save(teacher)
                count++
            }
        }
        workbook.close()
        return "$count ta o'qituvchi yaratildi"
    }

    // --- 7. STUDENTS ---
    @Transactional
    fun importStudents(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val sId = row.getCell(0)?.toString()?.trim() ?: continue
            val fName = row.getCell(1)?.toString()?.trim() ?: ""
            val gName = row.getCell(3)?.toString()?.trim() ?: ""

            val group = groupRepository.findByName(gName) ?: continue

            if (studentRepository.findById(sId).isEmpty) {
                studentRepository.save(Student(
                    studentId = sId,
                    fullName = fName,
                    faceId = "FACE_$sId",
                    group = group
                ))
                count++
            }
        }
        workbook.close()
        return "$count ta talaba qo'shildi"
    }

    // --- 8. LESSONS (Potok Logic) ---
    @Transactional
    fun importLessons(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val day = row.getCell(0)?.numericCellValue?.toInt() ?: continue
            val slot = row.getCell(1)?.numericCellValue?.toInt() ?: continue
            val sCode = row.getCell(2)?.toString()?.trim() ?: ""
            val rNo = row.getCell(3)?.toString()?.trim() ?: ""
            val bName = row.getCell(4)?.toString()?.trim() ?: ""
            val gNames = row.getCell(5)?.toString()?.trim() ?: ""
            val tUser = row.getCell(6)?.toString()?.trim() ?: ""
            val type = row.getCell(7)?.toString()?.trim()?.uppercase() ?: "PRACTICE"

            val timeslot = timeslotRepository.findByDayOfWeekAndSlotNumber(day, slot) ?: continue
            val bino = buildingRepository.findByName(bName) ?: continue
            val room = roomRepository.findByRoomNumberAndBuilding(rNo, bino) ?: continue
            val subject = subjectRepository.findByCode(sCode) ?: continue
            val teacher = teacherRepository.findByUserUsername(tUser) ?: continue

            val groupSet = gNames.split(",").mapNotNull { groupRepository.findByName(it.trim()) }.toMutableSet()

            lessonRepository.save(Lesson(
                subject = subject,
                teacher = teacher,
                groups = groupSet,
                type = LessonType.valueOf(type),
                timeslot = timeslot,
                room = room,
                isActive = true
            ))
            count++
        }
        workbook.close()
        return "$count ta dars kiritildi"
    }

    // --- 9. TEMPLATE GENERATOR ---
    fun generateSmartTemplate(sheets: Map<String, List<String>>, samples: Map<String, List<List<Any>>>): ByteArray {
        val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
        sheets.forEach { (name, headers) ->
            val sheet = workbook.createSheet(name)
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

            samples[name]?.forEachIndexed { rowIndex, sample ->
                val row = sheet.createRow(rowIndex + 1)
                sample.forEachIndexed { colIndex, value -> row.createCell(colIndex).setCellValue(value.toString()) }
            }
        }
        val out = java.io.ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()
        return out.toByteArray()
    }

    // --- 10. IMPORT EVERYTHING (DTO Variant) ---
    @Transactional
    fun importEverything(data: UniversityDataImport) {
        data.buildings.forEach { binoDto ->
            binoDto.rooms.forEach { xonaDto ->
                roomService.saveRoom(binoDto.name, xonaDto.roomNumber, xonaDto.capacity, xonaDto.isLab)
            }
        }

        data.subjects.forEach { fan ->
            if (subjectRepository.findByCode(fan.code) == null) {
                subjectRepository.save(Subject(
                    name = fan.name,
                    code = fan.code,
                    lectureHours = fan.lecture,
                    labHours = fan.lab
                ))
            }
        }

        data.groups.forEach { guruh ->
            if (groupRepository.findByName(guruh.name) == null) {
                groupRepository.save(StudentGroup(name = guruh.name, studentCount = guruh.studentCount))
            }
        }

        data.lessons.forEach { lDto ->
            val group = groupRepository.findByName(lDto.groupName)
            val subject = subjectRepository.findByCode(lDto.subjectCode)
            val teacher = teacherRepository.findByUserId(lDto.teacherName.toLong())

            if (group != null && subject != null) {
                lessonRepository.save(Lesson(
                    subject = subject,
                    teacher = teacher,
                    groups = mutableSetOf(group),
                    isActive = true
                ))
            }
        }
    }

    // --- 9. ALLOCATIONS (Kim qaysi guruhga dars o'tishi) ---
    @Transactional
    fun importAllocations(file: MultipartFile): String {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        var count = 0

        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            // 1. Ma'lumotlarni o'qish (Excel: 0-Username, 1-SubjectCode, 2-GroupName)
            val uName = row.getCell(0)?.toString()?.trim() ?: continue
            val sCode = row.getCell(1)?.toString()?.trim() ?: continue
            val gName = row.getCell(2)?.toString()?.trim() ?: ""

            // 2. Bog'liqliklarni tekshirish
            val teacher = teacherRepository.findByUserUsername(uName) ?: continue
            val subject = subjectRepository.findByCode(sCode) ?: continue
            val group = groupRepository.findByName(gName)

            // 3. Dublikatni tekshirish (Bir xil biriktiruv qayta qo'shilmasligi uchun)
            // Agar senda findByTeacherAndSubjectAndGroup metodi bo'lsa ishlat,
            // bo'lmasa shunchaki save qilaver (yoki unique constraint bo'lsa try-catch qil)
            subjectAllocationRepository.save(
                SubjectAllocation(
                    subject = subject,
                    teacher = teacher,
                    group = group
                )
            )
            count++
        }

        workbook.close()
        return "$count ta biriktirish (allocation) muvaffaqiyatli bajarildi"
    }
}