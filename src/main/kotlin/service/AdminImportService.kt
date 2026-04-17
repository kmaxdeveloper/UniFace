package com.uniface.service

import com.uniface.data.Role
import com.uniface.entity.*
import com.uniface.entity.matrix.Building
import com.uniface.entity.matrix.Department
import com.uniface.entity.matrix.Faculty
import com.uniface.entity.matrix.Room
import com.uniface.repository.*
import com.uniface.repository.matrix.*
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@Service
class AdminImportService(
    private val studentRepository: StudentRepository,
    private val groupRepository: StudentGroupRepository,
    private val facultyRepository: FacultyRepository,
    private val subjectRepository: SubjectRepository,
    private val departmentRepository: DepartmentRepository,
    private val teacherRepository: TeacherRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roomRepository: RoomRepository,
    private val buildingRepository: BuildingRepository,
    private val subjectAllocationRepository: SubjectAllocationRepository
) {

    // --- 1. TALABALAR IMPORTI ---
    // ====================================
    @Transactional
    fun importStudents(file: MultipartFile, facultyId: Long) {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        val faculty = facultyRepository.findById(facultyId).orElseThrow { RuntimeException("Fakultet topilmadi!") }
        val studentsToSave = mutableListOf<Student>()
        val processedGroups = mutableMapOf<String, StudentGroup>()

        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val sId = row.getCell(0)?.toString()?.trim() ?: continue
            val fName = row.getCell(1)?.stringCellValue ?: "Noma'lum"
            val fId = row.getCell(2)?.toString()?.trim() ?: "FACE_$sId"
            val gName = row.getCell(3)?.stringCellValue?.trim() ?: "GURUH_YOQ"

            val group = processedGroups.getOrPut(gName) {
                groupRepository.findByName(gName) ?: groupRepository.save(StudentGroup(gName, 0, faculty))
            }
            group.studentCount += 1
            studentsToSave.add(Student(sId, fName, fId, group, null))
        }
        studentRepository.saveAll(studentsToSave)
        groupRepository.saveAll(processedGroups.values)
        workbook.close()
    }

    // --- 2. FANLAR IMPORTI ---
    @Transactional
    fun importSubjects(file: MultipartFile, departmentId: Long) {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        val department = departmentRepository.findById(departmentId).orElseThrow { RuntimeException("Kafedra topilmadi!") }
        val subjectsToSave = mutableListOf<Subject>()

        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val sCode = row.getCell(0)?.toString()?.trim()
            val sName = row.getCell(1)?.stringCellValue?.trim() ?: continue
            val lHours = try { row.getCell(2).numericCellValue.toInt() } catch (e: Exception) { 0 }
            val labHrs = try { row.getCell(3).numericCellValue.toInt() } catch (e: Exception) { 0 }

            subjectsToSave.add(Subject(sName, sCode, lHours, labHrs, department))
        }
        subjectRepository.saveAll(subjectsToSave)
        workbook.close()
    }

    // --- 3. O'QITUVCHILAR IMPORTI ---
    @Transactional
    fun importTeachers(file: MultipartFile, facultyName: String, departmentName: String) {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        val teachersToSave = mutableListOf<Teacher>()

        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val fName = row.getCell(0)?.stringCellValue ?: continue
            val uName = row.getCell(1)?.toString()?.trim() ?: fName.replace(" ", "").lowercase()

            val userAccount = User(
                username = uName,
                password = passwordEncoder.encode("tatu1234"), // Default parol
                fullName = fName,
                role = Role.ROLE_TEACHER
            )

            val teacher = Teacher(
                fullName = fName,
                user = userAccount,
                department = departmentName,
                faculty = facultyName,
                status = true
            )
            userAccount.teacherProfile = teacher

            // Fanlarni bog'lash (C ustunida: "CSE101, MAT202")
            val codesStr = row.getCell(2)?.toString()?.trim() ?: ""
            if (codesStr.isNotEmpty()) {
                val codes = codesStr.split(",").map { it.trim() }
                val foundSubjects = subjectRepository.findAllByCodeIn(codes)
                teacher.subjects.addAll(foundSubjects)
            }
            teachersToSave.add(teacher)
        }
        teacherRepository.saveAll(teachersToSave)
        workbook.close()
    }

    @Transactional
    fun importInfrastructure(file: MultipartFile) {
        val workbook = WorkbookFactory.create(file.inputStream)

        // 1. BINOLARNI IMPORT QILISH (Sheet 0)
        val buildingSheet = workbook.getSheetAt(0)
        val buildingMap = mutableMapOf<String, Building>()

        for (i in 1..buildingSheet.lastRowNum) {
            val row = buildingSheet.getRow(i) ?: continue
            val bName = row.getCell(0)?.stringCellValue?.trim() ?: continue
            val floors = try { row.getCell(1).numericCellValue.toInt() } catch (e: Exception) { 1 }

            val building = buildingRepository.findByName(bName) ?: buildingRepository.save(
                Building(name = bName, floorCount = floors)
            )
            buildingMap[bName] = building
        }

        // 2. XONALARNI IMPORT QILISH (Sheet 1)
        // Excel: RoomNumber | Capacity | isLaboratory (Boolean) | BuildingName
        val roomSheet = workbook.getSheetAt(1)
        val roomsToSave = mutableListOf<Room>()

        for (i in 1..roomSheet.lastRowNum) {
            val row = roomSheet.getRow(i) ?: continue

            val rNumber = row.getCell(0)?.toString()?.trim() ?: continue
            val cap = try { row.getCell(1).numericCellValue.toInt() } catch (e: Exception) { 30 }
            val isLab = row.getCell(2)?.booleanCellValue ?: false
            val bName = row.getCell(3)?.stringCellValue?.trim() ?: continue

            val building = buildingMap[bName] ?: buildingRepository.findByName(bName)

            if (building != null) {
                // Sening repo-ngdagi metod orqali dublikat tekshiramiz
                val existingRoom = roomRepository.findByRoomNumberAndBuilding(rNumber, building)
                if (existingRoom == null) {
                    roomsToSave.add(
                        Room(
                            roomNumber = rNumber,
                            capacity = cap,
                            isLaboratory = isLab,
                            building = building
                        )
                    )
                }
            }
        }

        roomRepository.saveAll(roomsToSave)
        workbook.close()
    }

    @Transactional
    fun importAllocations(file: MultipartFile) {
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        val allocationsToSave = mutableListOf<SubjectAllocation>()

        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            val username = row.getCell(0)?.stringCellValue?.trim() ?: continue
            val subjectCode = row.getCell(1)?.toString()?.trim() ?: continue
            val groupName = row.getCell(2)?.stringCellValue?.trim()
            val patokName = row.getCell(3)?.stringCellValue?.trim()

            // 1. O'qituvchini topamiz (Sening repository'ngdagi findByUserUsername orqali)
            val teacher = teacherRepository.findByUserUsername(username) ?: continue

            // 2. Fanni topamiz
            val subject = subjectRepository.findByCode(subjectCode) ?: continue

            // 3. Guruhni topamiz (Agar guruh nomi berilgan bo'lsa)
            val group = groupName?.let { groupRepository.findByName(it) }

            // 4. Allocation yaratamiz
            val allocation = SubjectAllocation(
                subject = subject,
                teacher = teacher,
                group = group
            ).apply {
                this.isPatok = !patokName.isNullOrEmpty()
                this.patokName = patokName
            }

            allocationsToSave.add(allocation)
        }

        subjectAllocationRepository.saveAll(allocationsToSave)
        workbook.close()
    }

    @Transactional
    fun importOrgStructure(file: MultipartFile) {
        val workbook = WorkbookFactory.create(file.inputStream)

        // 1. FAKULTETLARNI IMPORT QILISH (Sheet 0)
        val facultySheet = workbook.getSheetAt(0)
        val facultyMap = mutableMapOf<String, Faculty>()

        for (i in 1..facultySheet.lastRowNum) {
            val row = facultySheet.getRow(i) ?: continue
            val fName = row.getCell(0)?.stringCellValue?.trim() ?: continue

            // Agar bazada bo'lsa olamiz, yo'q bo'lsa yangi yaratamiz
            val faculty = facultyRepository.findByName(fName) ?: facultyRepository.save(
                Faculty(name = fName)
            )
            facultyMap[fName] = faculty
        }

        // 2. KAFEDRALARNI IMPORT QILISH (Sheet 1)
        val deptSheet = workbook.getSheetAt(1)
        val deptsToSave = mutableListOf<Department>()

        for (i in 1..deptSheet.lastRowNum) {
            val row = deptSheet.getRow(i) ?: continue
            val dName = row.getCell(0)?.stringCellValue?.trim() ?: continue
            val fName = row.getCell(1)?.stringCellValue?.trim() ?: continue

            val faculty = facultyMap[fName] ?: facultyRepository.findByName(fName)

            if (faculty != null) {
                // Kafedra dublikat bo'lmasligi uchun tekshiramiz
                val existingDept = departmentRepository.findByName(dName)
                if (existingDept == null) {
                    deptsToSave.add(
                        Department(name = dName, faculty = faculty)
                    )
                }
            }
        }

        departmentRepository.saveAll(deptsToSave)
        workbook.close()
    }

    fun generateSmartTemplate(
        sheetHeaders: Map<String, List<String>>,
        sampleData: Map<String, List<List<String>>> = emptyMap()
    ): ByteArray {
        val workbook = XSSFWorkbook()

        sheetHeaders.forEach { (sheetName, headers) ->
            val sheet = workbook.createSheet(sheetName)

            // 1. Header qatori (Bold va chiroyli qilish ixtiyoriy, lekin tartib muhim)
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }

            // 2. Namuna ma'lumotlarini qo'shish (Admin qayerga nima yozishni ko'rsin)
            sampleData[sheetName]?.forEachIndexed { rowIndex, dataList ->
                val row = sheet.createRow(rowIndex + 1)
                dataList.forEachIndexed { colIndex, value ->
                    row.createCell(colIndex).setCellValue(value)
                }
            }

            // Ustunlar kengligini avtomat moslash
            headers.indices.forEach { sheet.autoSizeColumn(it) }
        }

        val out = ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()
        return out.toByteArray()
    }
}