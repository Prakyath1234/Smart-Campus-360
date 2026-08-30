# Stage 1: Build the Maven application
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create runtime image with LibreOffice
FROM eclipse-temurin:21-jre-noble
WORKDIR /app

# Install LibreOffice headless & fonts
RUN apt-get update && apt-get install -y --no-install-recommends \
    libreoffice-writer \
    libreoffice-impress \
    libreoffice-calc \
    libreoffice-common \
    fonts-dejavu \
    fonts-liberation \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Expose backend port
EXPOSE 8080

COPY --from=build /app/target/smart-campus-360-1.0.0-SNAPSHOT.jar app.jar

ENV SMARTTOOLS_LIBREOFFICE_PATH=/usr/bin/soffice
ENV SPRING_PROFILES_ACTIVE=mysql

ENTRYPOINT ["java", "-jar", "app.jar"]
