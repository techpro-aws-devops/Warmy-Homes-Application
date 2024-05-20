# Build aşaması için maven ve openjdk 17 kullan
FROM maven:3.8.1-openjdk-17-slim AS build

# Çalışma alanını oluştur
RUN mkdir -p /workspace
WORKDIR /workspace

# Proje bağımlılıklarını ve kaynak kodunu kopyala
COPY pom.xml /workspace/
COPY src /workspace/src

# Maven ile projeyi derle, testleri atlayarak
RUN mvn clean package -DskipTests

# Çalışma aşaması için openjdk 17 kullan
FROM openjdk:17-jdk-slim

# Derlenen jar dosyasını kopyala
COPY --from=build /workspace/target/*.jar app.jar

# Uygulamanın çalışacağı portu belirle
EXPOSE 8080

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"]
