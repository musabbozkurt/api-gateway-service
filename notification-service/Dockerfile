# Use a multi-stage build to optimize the image size
# Stage 1: Custom Java runtime (jlink).
# This stage is IDENTICAL in every service Dockerfile on purpose: BuildKit caches it by content, so
# all service images built on the same builder share one JRE layer (stored once on disk, not 13x).
# Do not change it in a single Dockerfile only; keep the module list in sync everywhere.
# Modules: Spring Boot, JPA/JDBC, Netty/Redis, RabbitMQ/Kafka, Actuator/Micrometer (jdk.jfr, jdk.management),
# JWT/TLS (jdk.crypto.ec, java.security.*), java.compiler (javax.lang.model.SourceVersion),
# jdk.net + java.net.http (Spring RestClient / Apache HttpClient inside Docker).
FROM maven:4.0.0-rc-5-eclipse-temurin-26-noble AS jre
RUN "$JAVA_HOME"/bin/jlink \
         --add-modules jdk.unsupported,java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.compiler,java.net.http,java.xml,java.logging,java.transaction.xa,java.security.sasl,jdk.crypto.ec,jdk.management,jdk.net,jdk.jfr \
         --strip-java-debug-attributes \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --exclude-files="**/legal/**,**.diz" \
         --output /mbjavaruntime

# Strip debug symbols from native libraries to reduce size
RUN find /mbjavaruntime -name '*.so' -exec strip --strip-unneeded {} + 2>/dev/null || true

# Stage 2: Build the application
FROM maven:4.0.0-rc-5-eclipse-temurin-26-noble AS build

# Set the working directory
WORKDIR /app

# Copy version-rules.xml to /app/ (parent of service dir) for versions-maven-plugin
COPY version-rules.xml /app/version-rules.xml

# Copy pom.xml first and resolve dependencies for better layer caching
COPY notification-service/pom.xml /app/notification-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/notification-service/pom.xml dependency:go-offline -B -q || true

# Copy source and build the application (tests skipped: Testcontainers/Mailpit need a Docker daemon in the build stage)
COPY notification-service/. /app/notification-service
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/notification-service/pom.xml clean package -DskipTests

# Stage 3: Create the final image
FROM debian:bookworm-slim

# Run as non-root (Debian default is root). The user and /app are created BEFORE any file is copied,
# so no recursive `chown -R` is needed: chown would copy the whole JRE into an extra image layer.
RUN groupadd --system --gid 1001 app \
    && useradd --system --uid 1001 --gid app --home-dir /app --no-create-home app \
    && mkdir /app \
    && chown app:app /app

ENV JAVA_HOME=/home/java/jdk25
ENV PATH=$JAVA_HOME/bin:$PATH

# Set the working directory for the final image
WORKDIR /app

# JRE stays root-owned: `app` only needs read/execute on it.
COPY --from=jre /mbjavaruntime $JAVA_HOME

# Copy the packaged jar file from the build stage (owned by `app` directly, no extra layer)
COPY --from=build --chown=app:app /app/notification-service/target/*.jar app.jar

USER app

# Command to run the application
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
