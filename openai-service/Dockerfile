# Use a multi-stage build to optimize the image size
# Stage 1: Build the application
FROM maven:3.9.9-ibm-semeru-21-jammy AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY . /app/openai-service

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/openai-service/pom.xml clean package -DskipTests

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules jdk.unsupported,java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
         --strip-java-debug-attributes \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /mbjavaruntime

# Stage 2: Create the final image
FROM openjdk:slim-stretch

ENV JAVA_HOME /home/java/jdk21
ENV PATH $JAVA_HOME/bin:$PATH

# Set the working directory for the final image
WORKDIR /app

COPY --from=build /mbjavaruntime $JAVA_HOME

# Copy the packaged jar file from the build stage
COPY --from=build /app/openai-service/target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
