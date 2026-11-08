FROM eclipse-temurin:21-jdk AS builder
WORKDIR /src
COPY . .
RUN ./gradlew --no-daemon :application:installDist

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 app
COPY --from=builder /src/application/build/install/application /opt/app
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -fs http://localhost:${SERVER_PORT:-8080}/health || exit 1
CMD ["/opt/app/bin/application"]
