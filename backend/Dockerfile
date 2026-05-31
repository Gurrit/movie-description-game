FROM eclipse-temurin:21-noble
WORKDIR /app

# Copy build files
COPY build.gradle.kts build.gradle.kts
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY gradle gradle
COPY src src

# Build the application
RUN ./gradlew build --no-daemon -x test

# Expose the port the app runs on
EXPOSE 8080

# Run the application
CMD ["./gradlew", "run"]
