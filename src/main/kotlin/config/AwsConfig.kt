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

    @Value("\${aws.accessKey:none}")
    private lateinit var accessKey: String

    @Value("\${aws.secretKey:none}")
    private lateinit var secretKey: String

    @Value("\${aws.region:us-east-1}")
    private lateinit var region: String

    @Bean
    fun rekognitionClient(): RekognitionClient {
        if (accessKey == "none" || secretKey == "none" || accessKey.isBlank()) {
            throw RuntimeException("AWS kalitlari (aws.accessKey / aws.secretKey) topilmadi! Docker komandasini tekshiring.")
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