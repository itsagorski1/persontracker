FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY src src
COPY html html
COPY index.html index.html

RUN chmod +x gradlew
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/install/persontracker /app/persontracker
COPY --from=build /app/html /app/html
COPY --from=build /app/index.html /app/index.html

ENV PORT=10000

EXPOSE 10000

CMD ["/app/persontracker/bin/persontracker"]
