package com.uniface.service.matrix

import com.uniface.dto.matrix.UniversityDataImport
import com.uniface.entity.matrix.Lesson // Import qo'shildi
import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.matrix.LessonRepository // Repo import qo'shildi
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ImportService(
    private val roomService: RoomEntryService,
    private val subjectRepo: SubjectRepository,
    private val groupRepo: StudentGroupRepository,
    private val lessonRepo: LessonRepository // 1. Bu yerga inject qildik ✅
) {
    @Transactional
    fun importEverything(data: UniversityDataImport) {
        // 1. Binolar va xonalarni saqlash
        data.buildings.forEach { binoDto ->
            binoDto.rooms.forEach { xonaDto ->
                // xonaDto.roomNumber ishlatamiz (DTO-dagi nom bilan bir xil)
                roomService.saveRoom(binoDto.name, xonaDto.roomNumber, xonaDto.capacity, xonaDto.isLab)
            }
        }

        // 2. Fanlarni saqlash
        data.subjects.forEach { fan ->
            if (subjectRepo.findByCode(fan.code) == null) {
                // Yangi konstruktoringga moslab:
                val newSubject = Subject(
                    name = fan.name,
                    code = fan.code,
                    lectureHours = fan.lecture, // DTO'dagi 'lecture'ni entity'dagi 'lectureHours'ga ulaymiz
                    labHours = fan.lab
                )
                subjectRepo.save(newSubject)
            }
        }

        // 3. Guruhlarni saqlash
        data.groups.forEach { guruh ->
            if (groupRepo.findByName(guruh.name) == null) {
                groupRepo.save(StudentGroup(name = guruh.name, studentCount = guruh.studentCount))
            }
        }

        // 4. Darslarni (Lesson) saqlash - "S" xatosi shu yerda bo'lishi mumkin
        data.lessons.forEach { lDto ->
            val group = groupRepo.findByName(lDto.groupName)
            val subject = subjectRepo.findByCode(lDto.subjectCode)

            if (group != null && subject != null) {
                // Lesson obyektini aniq tipda yarating
                val newLesson = Lesson(
                    subject = subject,
                    teacherName = lDto.teacherName,
                    studentGroup = group
                )
                lessonRepo.save(newLesson) // Endi "S" parametrini aniqlay oladi ✅
            }
        }
    }
}