package com.uniface.service

import com.uniface.data.Role
import com.uniface.dto.FaceResponse
import com.uniface.entity.Attendance
import com.uniface.entity.User
import com.uniface.repository.AttendanceRepository
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.StudentRepository
import com.uniface.repository.SubjectRepository
import com.uniface.repository.TeacherRepository
import com.uniface.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.model.*
import java.time.LocalDate
import java.time.LocalTime

@Service
class FaceService(
    private val rekClient: RekognitionClient,
    private val studentRepository: StudentRepository,   // Baza bilan ishlash uchun
    private val attendanceRepository: AttendanceRepository, // Davomatni saqlash uchun
    private val subjectRepository: SubjectRepository,
    private val groupRepository: StudentGroupRepository,
    private val passwordEncoder: PasswordEncoder,
    private val teacherRepository: TeacherRepository,
    private val userRepository: UserRepository
) {
    private val collectionId = "UniFaceCollection"

    // Talabani ro'yxatga olish (AWS + Local DB)
    @Transactional // ✅ Agar biror joyda xato bo'lsa, hatto User ham bazaga kirmaydi
    fun registerFace(
        studentId: String,
        fullName: String,
        groupId: Long,
        imageBytes: ByteArray
    ): FaceResponse {
        // 1. Guruhni tekshirish (Erta xatolikni ushlash)
        val group = groupRepository.findById(groupId).orElseThrow {
            IllegalArgumentException("Guruh topilmadi: ID=$groupId")
        }
        if (userRepository.existsByUsername(studentId)) {
            throw IllegalArgumentException("Bu ID bilan talaba allaqachon ro'yxatdan o'tgan!")
        }

        // 2. User yaratish (studentId ni username sifatida ishlatish - tavsiyamiz)
        val newUser = User().apply {
            this.username = studentId
            this.password = passwordEncoder.encode("12345") // "12345" shifrlanadi
            this.role = Role.ROLE_STUDENT
        }
        val savedUser = userRepository.save(newUser)

        // 3. AWS Rekognition Indexing
        val request = IndexFacesRequest.builder()
            .collectionId(collectionId)
            .externalImageId(studentId)
            .image(Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build())
            .build()

        val response = rekClient.indexFaces(request)

        // 4. Tekshirish va Saqlash
        if (response.faceRecords().isNotEmpty()) {
            val awsFaceId = response.faceRecords()[0].face().faceId()

            val newStudent = com.uniface.entity.Student().apply {
                this.studentId = studentId
                this.fullName = fullName
                this.faceId = awsFaceId
                this.group = group
                this.user = savedUser
            }

            studentRepository.save(newStudent)
            return FaceResponse(true, "Talaba muvaffaqiyatli saqlandi", studentId)
        } else {
            // 🚨 MUHIM: Bu xato Spring'ga yetib borishi kerak, shunda Rollback ishlaydi!
            throw RuntimeException("Rasmdan yuz topilmadi. Tranzaksiya bekor qilindi.")
        }
    }

    // Talabani tanish va Davomatga yozish
    fun identifyStudent(
        imageBytes: ByteArray,
        subjectId: Long,
        groupId: Long,
        teacherId: Long
    ): FaceResponse {
        return try {
            // 1. Bazadan fan va guruhni tekshirib olamiz
            val subject = subjectRepository.findById(subjectId).orElse(null)
                ?: return FaceResponse(false, "Fan topilmadi")
            val group = groupRepository.findById(groupId).orElse(null)
                ?: return FaceResponse(false, "Guruh topilmadi")
            val teacher = teacherRepository.findById(teacherId).orElse(null)
                ?: return FaceResponse(false, "O'qituvchi topilmadi")

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
                            this.teacher = teacher
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
        teacherId: Long
    ): Map<String, Any> {
        val subject = subjectRepository.findById(subjectId).orElseThrow { Exception("Fan topilmadi") }
        val group = groupRepository.findById(groupId).orElseThrow { Exception("Guruh topilmadi") }
        val teacher = teacherRepository.findById(teacherId).orElseThrow { Exception("O'qituvchi topilmadi") }

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

                            val attendance = Attendance(
                                student,
                                subject,
                                group,
                                teacher,
                                null // Lesson bu yerda null bo'ladi
                            )
                            attendanceRepository.save(attendance)
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

    @Transactional
    fun deleteStudent(studentId: String): String {
        return try {
            val student = studentRepository.findById(studentId).orElseThrow {
                Exception("Talaba topilmadi")
            }

            // 1. AWS dan yuzni o'chiramiz
            val deleteRequest = DeleteFacesRequest.builder()
                .collectionId(collectionId)
                .faceIds(student.faceId)
                .build()
            rekClient.deleteFaces(deleteRequest)

            // 2. Bazadan Studentni o'chiramiz
            val userId = student.user?.id
            studentRepository.delete(student)

            // 3. Bog'langan User (Login)ni ham o'chiramiz
            userId?.let { userRepository.deleteById(it) }

            "Talaba va uning barcha ma'lumotlari o'chirildi"
        } catch (e: Exception) {
            throw RuntimeException("O'chirishda xatolik: ${e.message}")
        }
    }
}