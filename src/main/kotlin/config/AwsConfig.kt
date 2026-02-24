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
class AwsConfig(
    @Value("\${aws.accessKey}") private val accessKey: String,
    @Value("\${aws.secretKey}") private val secretKey: String,
    @Value("\${aws.region}") private val region: String
) {
    @Bean
    fun rekognitionClient(): RekognitionClient {
        return RekognitionClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .httpClient(ApacheHttpClient.builder().build())
            .build()
    }
}