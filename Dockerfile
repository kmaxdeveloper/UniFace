# FROM openjdk:17-jdk-slim
# COPY build/libs/*.jar app.jar
# ENTRYPOINT ["java", "-jar", "/app.jar"]

# Eski o'chgan qator o'rniga mana buni qo'y:
FROM eclipse-temurin:17-jdk-jammy
# Qolgan hamma narsa o'zgarishsiz qoladi
COPY build/libs/UniFace-1.0-SNAPSHOT.jar app.jar
#COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]