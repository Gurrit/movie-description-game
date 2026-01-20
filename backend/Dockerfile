FROM eclipse-temurin:21-noble as BUILD
WORKDIR /app

COPY build.gradle.kts build.gradle.kts
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY gradle gradle
COPY src src

ENV REDIS_HOST mdg-redis

RUN ./gradlew build --no-daemon -x test

CMD ./gradlew run

#FROM openjdk:11-jdk as BACKEND

#WORKDIR /app
#COPY --from=BUILD /app/build/ ./build/
#COPY ./gradlew ./gradlew
#COPY gradle gradle
#RUN ls

#RUN ./gradlew run

