package com.uniface.unit

import com.uniface.entity.User
import com.uniface.loader.DataLoader
import com.uniface.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class DataLoaderTest {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    lateinit var dataLoader: DataLoader

    @Test
    fun `should create admin if it does not exist`() {
        // Given
        val adminUsername = "mainAdmin"
        val encodedPass = "encoded_pass"

        every { userRepository.findByUsername(adminUsername) } returns null
        every { passwordEncoder.encode(any()) } returns encodedPass
        every { userRepository.save(any()) } returns mockk()

        dataLoader.run()

        verify(exactly = 1) { userRepository.save(any()) }
        verify { passwordEncoder.encode("mainAdmin") }
    }

    @Test
    fun `should not create admin if it already exists`() {
        // Given
        val adminUsername = "mainAdmin"
        val existingUser = User(username = adminUsername, password = "...", fullName = "Admin", role = mockk())

        every { userRepository.findByUsername(adminUsername) } returns existingUser

        dataLoader.run()

        verify(exactly = 0) { userRepository.save(any()) }
    }
}