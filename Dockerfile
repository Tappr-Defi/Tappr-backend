# ... (previous lines)
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------
# Fix: Use eclipse-temurin instead of openjdk:17
# ---------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine

COPY --from=build ./target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dserver.port=8081", "app.jar"]