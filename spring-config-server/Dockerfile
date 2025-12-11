# Use a multi-stage build to optimize the image size
# Stage 1: Build the application
FROM maven:4.0.0-rc-5-ibm-semeru-25-noble AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY . /app/spring-config-server

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/spring-config-server/pom.xml clean package -DskipTests

# Create a custom Java runtime - Added java.compiler module to provide javax.lang.model.SourceVersion
RUN "$JAVA_HOME"/bin/jlink \
         --add-modules jdk.unsupported,java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.compiler \
         --strip-java-debug-attributes \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /mbjavaruntime

# Stage 2: Create the final image
FROM ubuntu:25.10

ENV JAVA_HOME=/home/java/jdk25
ENV PATH=$JAVA_HOME/bin:$PATH

# Set the working directory for the final image
WORKDIR /app

COPY --from=build /mbjavaruntime $JAVA_HOME

# Copy the packaged jar file from the build stage
COPY --from=build /app/spring-config-server/target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
