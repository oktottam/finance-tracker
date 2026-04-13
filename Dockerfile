# Stage 1: Build bằng Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
# Copy pom.xml và source code vào container
COPY pom.xml .
COPY src ./src
# Build ra file jar (bỏ qua unit test để chạy cho nhanh)
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy file jar từ stage build sang stage này
COPY --from=build /app/target/*.jar app.jar

# Chạy app
ENTRYPOINT ["java", "-jar", "app.jar"]