package com.uniface.service

import com.uniface.dto.FaceResponse
import com.uniface.entity.Attendance
import com.uniface.repository.AttendanceRepository
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.StudentRepository
import com.uniface.repository.SubjectRepository
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class FaceService(
    private val rekClient: RekognitionClient,
    private val studentRepository: StudentRepository,   // Baza bilan ishlash uchun
    private val attendanceRepository: AttendanceRepository, // Davomatni saqlash uchun
    private val subjectRepository: SubjectRepository,
    private val groupRepository: StudentGroupRepository
) {
    private val collectionId = "UniFaceCollection"

    // Talabani ro'yxatga olish (AWS + Local DB)
    fun registerFace(
        studentId: String,
        fullName: String,
        groupId: Long, // Endi String emas, Long ID kelyapti
        imageBytes: ByteArray
    ): FaceResponse {
        return try {
            // 1. Bazadan guruhni qidiramiz
            val group = groupRepository.findById(groupId).orElse(null)
                ?: return FaceResponse(false, "Xatolik: ID=$groupId bo'lgan guruh topilmadi")

            // 2. AWS Rekognition'ga yuzni yuboramiz
            val request = IndexFacesRequest.builder()
                .collectionId(collectionId)
                .externalImageId(studentId)
                .image(Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build())
                .build()

            val response = rekClient.indexFaces(request)

            if (response.faceRecords().isNotEmpty()) {
                val awsFaceId = response.faceRecords()[0].face().faceId()

                // 3. Bazaga yangi talabani saqlaymiz (Obyekt ko'rinishida)
                val newStudent = com.uniface.entity.Student().apply {
                    this.studentId = studentId
                    this.fullName = fullName
                    this.faceId = awsFaceId
                    this.group = group // Topilgan guruh obyektini beramiz
                }

                studentRepository.save(newStudent)

                FaceResponse(true, "Talaba muvaffaqiyatli saqlandi: $fullName", studentId)
            } else {
                FaceResponse(false, "Rasmdan yuz topilmadi. Iltimos, tiniqroq rasm yuklang.")
            }
        } catch (e: Exception) {
            FaceResponse(false, "Xatolik yuz berdi: ${e.message}")
        }
    }

    // Talabani tanish va Davomatga yozish
    fun identifyStudent(
        imageBytes: ByteArray,
        subjectId: Long,
        groupId: Long,
        teacherName: String
    ): FaceResponse {
        return try {
            // 1. Bazadan fan va guruhni tekshirib olamiz
            val subject = subjectRepository.findById(subjectId).orElse(null)
                ?: return FaceResponse(false, "Fan topilmadi")
            val group = groupRepository.findById(groupId).orElse(null)
                ?: return FaceResponse(false, "Guruh topilmadi")

            val request = SearchFacesByImageRequest.builder()
                .collectionId(collectionId)
                .faceMatchThreshold(90F)
                .maxFaces(1)
                .image(Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build())
                .build()

            val response = rekClient.searchFacesByImage(request)
            val matches = response.faceMatches()

            if (matches.isNotEmpty()) {
                val awsFaceId = matches[0].face().faceId()
                val similarity = matches[0].similarity()

                // 2. Bazadan talabani topamiz
                val student = studentRepository.findByFaceId(awsFaceId)

                if (student != null) {
                    // 3. Dublikatni tekshirish (Bugun shu fanga kelganmi?)
                    val todayStart = LocalDate.now().atStartOfDay()
                    val todayEnd = LocalDate.now().atTime(LocalTime.MAX)

                    val alreadyMarked = attendanceRepository.existsByStudentAndSubjectToday(
                        student, subject, todayStart, todayEnd
                    )

                    if (!alreadyMarked) {
                        // 4. Davomat jadvaliga hamma ma'lumot bilan saqlaymiz
                        val attendance = Attendance().apply {
                            this.student = student
                            this.subject = subject
                            this.group = group
                            this.teacherName = teacherName
                        }
                        attendanceRepository.save(attendance)
                        FaceResponse(true, "Xush kelibsiz, ${student.fullName}!", student.studentId, similarity)
                    } else {
                        FaceResponse(true, "Siz allaqachon belgilangansiz, ${student.fullName}", student.studentId)
                    }
                } else {
                    FaceResponse(false, "AWS tanidi, lekin bazada bunday talaba yo'q")
                }
            } else {
                FaceResponse(false, "Tizim bu yuzni tanimadi!")
            }
        } catch (e: Exception) {
            FaceResponse(false, "Xatolik: ${e.message}")
        }
    }

    fun createCollection(): String {
        return try {
            val request = CreateCollectionRequest.builder()
                .collectionId(collectionId)
                .build()
            rekClient.createCollection(request)
            "Collection yaratildi!"
        } catch (e: Exception) {
            "Xatolik: ${e.message}"
        }
    }

    fun processBulkAttendance(
        imageBytes: ByteArray,
        subjectId: Long,
        groupId: Long,
        teacherName: String
    ): Map<String, Any> {
        val subject = subjectRepository.findById(subjectId).orElseThrow { Exception("Fan topilmadi") }
        val group = groupRepository.findById(groupId).orElseThrow { Exception("Guruh topilmadi") }

        val todayStart = LocalDate.now().atStartOfDay()
        val todayEnd = LocalDate.now().atTime(LocalTime.MAX)

        // 1. AWS Rekognition orqali rasm ichidagi hamma yuzlarni qidirish
        val sdkBytes = SdkBytes.fromByteArray(imageBytes)
        val image = Image.builder().bytes(sdkBytes).build()
        val searchRequest = SearchFacesByImageRequest.builder()
            .collectionId("UniFaceCollection")
            .image(image)
            .faceMatchThreshold(80f)
            .maxFaces(100)
            .build()

        val response = rekClient.searchFacesByImage(searchRequest)
        val identifiedStudents = mutableListOf<String>()

        // 2. Topilgan har bir yuz uchun
        response.faceMatches().forEach { match ->
            val faceId = match.face().faceId()
            val student = studentRepository.findByFaceId(faceId)

            if (student != null) {
                // 3. Dublikatni tekshirish (bugun shu fan uchun)
                val isAlreadyMarked = attendanceRepository.existsByStudentAndSubjectToday(
                    student, subject, todayStart, todayEnd
                )

                if (!isAlreadyMarked) {
                    val attendance = Attendance(student, subject, group, teacherName)
                    attendanceRepository.save(attendance)
                    identifiedStudents.add(student.fullName)
                }
            }
        }

        return mapOf(
            "message" to "Davomat yakunlandi",
            "detectedCount" to response.faceMatches().size,
            "identifiedCount" to identifiedStudents.size,
            "list" to identifiedStudents
        )
    }
}