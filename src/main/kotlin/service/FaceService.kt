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

        val identifiedStudents = mutableListOf<String>()
        val alreadyMarkedStudents = mutableListOf<String>()

        // 1. Rasmdagi HAMMA yuzlarni topamiz
        val detectRequest = DetectFacesRequest.builder()
            .image(Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build())
            .attributes(Attribute.DEFAULT)
            .build()
        val detectedFaces = rekClient.detectFaces(detectRequest).faceDetails()

        println("🔍 DetectFaces: ${detectedFaces.size} ta yuz topildi")

        val bufferedImage = javax.imageio.ImageIO.read(imageBytes.inputStream())
        val imgWidth = bufferedImage.width
        val imgHeight = bufferedImage.height

        println("📐 Rasm o'lchami: ${imgWidth}x${imgHeight}")

        detectedFaces.forEach { faceDetail ->
            try {
                val box = faceDetail.boundingBox()
                println("📦 BoundingBox: left=${box.left()}, top=${box.top()}, w=${box.width()}, h=${box.height()}")

                val left   = (box.left() * imgWidth).toInt().coerceAtLeast(0)
                val top    = (box.top() * imgHeight).toInt().coerceAtLeast(0)
                val width  = (box.width() * imgWidth).toInt().coerceAtMost(imgWidth - left)
                val height = (box.height() * imgHeight).toInt().coerceAtMost(imgHeight - top)

                println("✂️ Crop: left=$left, top=$top, width=$width, height=$height")

                val croppedImage = bufferedImage.getSubimage(left, top, width, height)
                val outputStream = java.io.ByteArrayOutputStream()
                javax.imageio.ImageIO.write(croppedImage, "jpg", outputStream)
                val croppedBytes = outputStream.toByteArray()

                println("📸 Crop bayt hajmi: ${croppedBytes.size}")

                val searchRequest = SearchFacesByImageRequest.builder()
                    .collectionId(collectionId)
                    .image(Image.builder().bytes(SdkBytes.fromByteArray(croppedBytes)).build())
                    .faceMatchThreshold(70f) // ✅ pastroq threshold
                    .maxFaces(1)
                    .build()

                val matches = rekClient.searchFacesByImage(searchRequest).faceMatches()
                println("✅ Matches: ${matches.size}")

                if (matches.isNotEmpty()) {
                    val faceId = matches[0].face().faceId()
                    val similarity = matches[0].similarity()
                    println("👤 FaceId: $faceId, Similarity: $similarity")

                    val student = studentRepository.findByFaceId(faceId)
                    println("🎓 Student: ${student?.fullName ?: "topilmadi"}")

                    if (student != null) {
                        val isAlreadyMarked = attendanceRepository.existsByStudentAndSubjectToday(
                            student, subject, todayStart, todayEnd
                        )
                        if (!isAlreadyMarked) {
                            attendanceRepository.save(Attendance(student, subject, group, teacherName))
                            identifiedStudents.add("✅ ${student.fullName}")
                        } else {
                            alreadyMarkedStudents.add("🔄 ${student.fullName} (allaqachon belgilangan)")
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ Xato: ${e.message}")
            }
        }

        val allList = identifiedStudents + alreadyMarkedStudents

        return mapOf(
            "message" to "Davomat yakunlandi. Yangi: ${identifiedStudents.size}",
            "detectedCount" to detectedFaces.size,
            "identifiedCount" to identifiedStudents.size,
            "alreadyMarkedCount" to alreadyMarkedStudents.size,
            "list" to allList
        )
    }
}