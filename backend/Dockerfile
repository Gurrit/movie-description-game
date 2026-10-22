# Build with the repository root as context: the API contract in /api is needed to generate code.
#   docker build -f backend/Dockerfile .

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY api api
COPY backend/gradlew backend/settings.gradle.kts backend/build.gradle.kts backend/
COPY backend/gradle backend/gradle
COPY backend/src backend/src
WORKDIR /workspace/backend
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/backend/build/libs/app.jar app.jar
USER app
EXPOSE 8080
# Container-aware heap sizing; leaves room for the JVM's own memory on a small host
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60"
ENTRYPOINT ["java", "-jar", "app.jar"]
