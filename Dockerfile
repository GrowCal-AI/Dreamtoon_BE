# Build stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Gradle 캐시 활용을 위해 의존성 먼저 다운로드
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY . .
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", \
    "-Xms512m", "-Xmx1g", \
    "-jar", \
    "app.jar"]
