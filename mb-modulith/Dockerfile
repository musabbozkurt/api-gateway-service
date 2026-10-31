# Multi-stage build: Maven compile + jlink runtime, slim Debian runtime image.
# Build context: api-gateway-service repository root (see docker-compose.yml).
FROM maven:4.0.0-rc-5-eclipse-temurin-26-noble AS build

WORKDIR /app

COPY version-rules.xml /app/version-rules.xml
COPY mb-modulith/pom.xml /app/mb-modulith/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/mb-modulith/pom.xml dependency:go-offline -B -q || true

COPY mb-modulith/. /app/mb-modulith
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/mb-modulith/pom.xml clean package -DskipTests

RUN "$JAVA_HOME"/bin/jlink \
         --add-modules jdk.unsupported,java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.compiler,java.net.http,java.xml,java.logging,java.transaction.xa,java.security.sasl,jdk.crypto.ec,jdk.management,jdk.net,jdk.jfr \
         --strip-java-debug-attributes \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --exclude-files="**/legal/**,**.diz" \
         --output /mbjavaruntime

RUN find /mbjavaruntime -name '*.so' -exec strip --strip-unneeded {} + 2>/dev/null || true

FROM debian:bookworm-slim

ENV JAVA_HOME=/home/java/jdk26
ENV PATH=$JAVA_HOME/bin:$PATH

WORKDIR /app

COPY --from=build /mbjavaruntime $JAVA_HOME
COPY --from=build /app/mb-modulith/target/*.jar app.jar

# Run as non-root (Debian default is root); app only needs read/exec on JRE and jar.
RUN groupadd --system --gid 1001 app \
    && useradd --system --uid 1001 --gid app --home-dir /app --no-create-home app \
    && chown -R app:app /app "$JAVA_HOME"

USER app

EXPOSE 8090

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
