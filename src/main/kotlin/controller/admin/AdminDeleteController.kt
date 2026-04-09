package com.uniface.controller.admin

import com.uniface.service.matrix.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class AdminDeleteController(private val adminService: AdminService) {

    @DeleteMapping("/delete-building/{id}")
    fun deleteBuilding(@PathVariable id: Long) = try {
        adminService.deleteBuilding(id)
        ResponseEntity.ok("Bino muvaffaqiyatli o'chirildi")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @DeleteMapping("/delete-room/{id}")
    fun deleteRoom(@PathVariable id: Long) = try {
        adminService.deleteRoom(id)
        ResponseEntity.ok("Xona muvaffaqiyatli o'chirildi")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @DeleteMapping("/delete-depart/{id}")
    fun deleteDepartment(@PathVariable id: Long) = try {
        adminService.deleteDepartment(id)
        ResponseEntity.ok("Kafedra muvaffaqiyatli o'chirildi")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @DeleteMapping("/delete-faculty/{id}")
    fun deleteFaculty(@PathVariable id: Long) = try {
        adminService.deleteFaculty(id)
        ResponseEntity.ok("Fakultet muvaffaqiyatli o'chirildi")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @DeleteMapping("/delete-student/{id}")
    fun deleteStudent(@PathVariable id: String) = try {
        adminService.deleteStudent(id)
        ResponseEntity.ok("Talaba muvaffaqiyatli o'chirildi")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @DeleteMapping("/delete-teacher/{id}")
    fun deleteTeacher(@PathVariable id: Long) = try {
        adminService.deleteTeacher(id)
        ResponseEntity.ok("O'qituvchi muvaffaqiyatli o'chirildi")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @DeleteMapping("/delete-group/{id}")
    fun deleteGroup(@PathVariable id: Long) = try {
        adminService.deleteGroup(id)
        ResponseEntity.ok("Guruh muvaffaqiyatli o'chirildi")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }

    @DeleteMapping("/delete-subject/{id}")
    fun deleteSubject(@PathVariable id: Long) = try {
        adminService.deleteSubject(id)
        ResponseEntity.ok("Fan muvaffaqiyatli o'chirildi")
    } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }
}