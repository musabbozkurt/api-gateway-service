# Use a multi-stage build to optimize the image size
# Stage 1: Build the application
FROM maven:4.0.0-rc-5-ibm-semeru-25-noble AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml first and resolve dependencies for better layer caching
COPY pom.xml /app/openai-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/openai-service/pom.xml dependency:go-offline -B -q || true

# Copy source and build the application
COPY . /app/openai-service
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/openai-service/pom.xml clean package

# Create a custom Java runtime - Added java.compiler module to provide javax.lang.model.SourceVersion
RUN "$JAVA_HOME"/bin/jlink \
         --add-modules jdk.unsupported,java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.compiler \
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

ENV JAVA_HOME=/home/java/jdk25
ENV PATH=$JAVA_HOME/bin:$PATH

# Set the working directory for the final image
WORKDIR /app

COPY --from=build /mbjavaruntime $JAVA_HOME

# Copy the packaged jar file from the build stage
COPY --from=build /app/openai-service/target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
