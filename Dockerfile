FROM eclipse-temurin:21-jdk AS builder
WORKDIR /src
COPY . .
RUN ./gradlew --no-daemon :application:installDist

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 app
COPY --from=builder /src/application/build/install/application /opt/app
USER app
EXPOSE 8080
CMD ["/opt/app/bin/application"]
