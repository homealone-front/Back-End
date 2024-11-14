# 웹훅 테스트2
# 베이스 이미지
FROM openjdk:17-jdk-alpine

# Gradle 소스 코드 복사
COPY gradlew .
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .

# 빌드 인자
ARG JAR_FILE=build/libs/*.jar

# docker 이미지 복사
COPY ${JAR_FILE} app.jar

# 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=test","-jar", "/app.jar"]
