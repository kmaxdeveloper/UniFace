package com.uniface.service

import com.uniface.entity.Patok
import com.uniface.repository.GroupRepository
import com.uniface.repository.PatokRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class PatokService(
    private val patokRepository: PatokRepository,
    private val groupRepository: GroupRepository
) {
    // Hammasini olish
    fun getAllPatoks(): List<Patok> = patokRepository.findAll()

    // Bitta patokni ID bo'yicha olish
    fun getPatokById(id: Long): Patok = patokRepository.findById(id)
        .orElseThrow { RuntimeException("Patok topilmadi!") }

    // Yangi Patok yaratish va guruhlarni unga biriktirish
    @Transactional
    fun createPatok(name: String, groupIds: List<Long>): Patok {
        val patok = patokRepository.save(Patok(name = name))
        val groups = groupRepository.findAllById(groupIds)
        groups.forEach {
            it.patok = patok
        }
        groupRepository.saveAll(groups)
        return patok
    }

    // Patokni o'chirish (guruhlarni ozod qilish)
    @Transactional
    fun deletePatok(id: Long) {
        val patok = getPatokById(id)
        patok.groups.forEach { it.patok = null }
        groupRepository.saveAll(patok.groups)
        patokRepository.delete(patok)
    }
}