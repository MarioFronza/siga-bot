FROM eclipse-temurin:25-jre
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/* \
  && useradd --system --uid 10001 app
ADD application/build/distributions/application.tar /opt/
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -fs http://localhost:${SERVER_PORT:-8080}/health || exit 1
CMD ["/opt/application/bin/application"]
