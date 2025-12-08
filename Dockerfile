# ---------------------------------------------------
# 1. Build Stage
# ---------------------------------------------------
# We use a Maven image that has JDK 17 installed to build the app
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory inside the container
WORKDIR /app

# Copy only the POM first (to cache dependencies)
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy the actual source code
COPY src ./src

# Build the JAR (Skip tests to speed up deployment)
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------
# 2. Run Stage
# ---------------------------------------------------
# We use a lighter JRE image just to run the app
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the JAR from the 'build' stage above
# Note: Ensure the path matches where Maven outputs the jar (usually /target)
COPY --from=build /app/target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8082

# Run the application
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT:-8082} app.jar"]