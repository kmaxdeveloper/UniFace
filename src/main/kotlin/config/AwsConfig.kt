package com.uniface.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.rekognition.RekognitionClient

@Configuration
class AwsConfig {

    @Value("\${aws.accessKey}")
    private lateinit var accessKey: String

    @Value("\${aws.secretKey}")
    private lateinit var secretKey: String

    @Value("\${aws.region:us-east-1}") // Default qiymat ham berib ketamiz
    private lateinit var region: String

    @Bean
    fun rekognitionClient(): RekognitionClient {
        // Logika: Agar qiymatlar baribir bo'sh kelsa, xatoni aniq bilamiz
        if (accessKey.isBlank() || secretKey.isBlank()) {
            throw RuntimeException("AWS Credentials are missing in application.properties!")
        }

        return RekognitionClient.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .httpClient(ApacheHttpClient.builder().build())
            .build()
    }
}