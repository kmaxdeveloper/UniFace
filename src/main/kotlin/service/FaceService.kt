package com.uniface.service

import com.uniface.dto.FaceResponse
import com.uniface.entity.Attendance
import com.uniface.repository.AttendanceRepository
import com.uniface.repository.StudentRepository
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.model.*
import java.time.LocalDateTime

@Service
class FaceService(
    private val rekClient: RekognitionClient,
    private val studentRepository: StudentRepository,   // Baza bilan ishlash uchun
    private val attendanceRepository: AttendanceRepository // Davomatni saqlash uchun
) {
    private val collectionId = "UniFaceCollection"

    // Talabani ro'yxatga olish (AWS + Local DB)
    fun registerFace(studentId: String, fullName: String, groupName: String, imageBytes: ByteArray): FaceResponse {
        return try {
            val request = IndexFacesRequest.builder()
                .collectionId(collectionId)
                .externalImageId(studentId)
                .image(Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build())
                .build()

            val response = rekClient.indexFaces(request)

            if (response.faceRecords().isNotEmpty()) {
                val awsFaceId = response.faceRecords()[0].face().faceId()

                // 1. AWS'ga qo'shilgach, o'zimizning bazaga ham hamma ma'lumotini saqlaymiz
                val newStudent = com.uniface.entity.Student(
                    studentId = studentId,
                    fullName = fullName,
                    groupName = groupName,
                    faceId = awsFaceId
                )
                studentRepository.save(newStudent)

                FaceResponse(true, "Talaba muvaffaqiyatli saqlandi: $fullName", studentId)
            } else {
                FaceResponse(false, "Rasmdan yuz topilmadi")
            }
        } catch (e: Exception) {
            FaceResponse(false, "Xatolik: ${e.message}")
        }
    }

    // Talabani tanish va Davomatga yozish
    fun identifyStudent(imageBytes: ByteArray): FaceResponse {
        return try {
            val request = SearchFacesByImageRequest.builder()
                .collectionId(collectionId)
                .faceMatchThreshold(90F)
                .maxFaces(1)
                .image(Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build())
                .build()

            val response = rekClient.searchFacesByImage(request)
            val matches = response.faceMatches()

            if (matches.isNotEmpty()) {
                val awsFaceId = matches[0].face().faceId() // AWS qaytargan unikal FaceId
                val similarity = matches[0].similarity()

                // 2. Bazamizdan shu FaceId ga ega talabani qidiramiz
                val student = studentRepository.findByFaceId(awsFaceId)

                if (student != null) {
                    // 3. Talaba topildi! Uni davomat jadvaliga yozib qo'yamiz
                    attendanceRepository.save(Attendance(student = student, timestamp = LocalDateTime.now()))

                    FaceResponse(true, "Xush kelibsiz, ${student.fullName}!", student.studentId, similarity)
                } else {
                    FaceResponse(false, "AWS tanidi, lekin baza ma'lumot topilmadi (FaceId: $awsFaceId)")
                }
            } else {
                FaceResponse(false, "Tizim bu yuzni tanimadi!")
            }
        } catch (e: Exception) {
            FaceResponse(false, "Tizim xatosi: ${e.message}")
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
}