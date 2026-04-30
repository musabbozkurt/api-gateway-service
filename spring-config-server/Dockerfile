# Use a multi-stage build to optimize the image size
# Stage 1: Build the application
FROM maven:4.0.0-rc-5-ibm-semeru-25-noble AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml first and resolve dependencies for better layer caching
COPY pom.xml /app/spring-config-server/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/spring-config-server/pom.xml dependency:go-offline -B -q || true

# Copy source and build the application
COPY . /app/spring-config-server
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/spring-config-server/pom.xml clean package

# Create a custom Java runtime - Added java.compiler module to provide javax.lang.model.SourceVersion
# Added jdk.net and java.net.http because Spring RestClient / Apache HttpClient may require
# jdk.net.Sockets and related networking classes at runtime inside Docker
RUN "$JAVA_HOME"/bin/jlink \
         --add-modules jdk.unsupported,jdk.net,java.net.http,java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.compiler \
         --strip-java-debug-attributes \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --exclude-files="**/legal/**,**.diz" \
         --output /mbjavaruntime

# Strip debug symbols from native libraries to reduce size
RUN find /mbjavaruntime -name '*.so' -exec strip --strip-unneeded {} + 2>/dev/null || true

# Stage 2: Create the final image
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/home/java/jdk25
ENV PATH=$JAVA_HOME/bin:$PATH

# Set the working directory for the final image
WORKDIR /app

COPY --from=build /mbjavaruntime $JAVA_HOME

# Copy the packaged jar file from the build stage
COPY --from=build /app/spring-config-server/target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
