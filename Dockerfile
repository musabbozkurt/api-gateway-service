FROM openjdk:11-jre
EXPOSE 8080
ADD target/api-gateway-service-project.jar api-gateway-service-project.jar
ENTRYPOINT ["java","-jar","/api-gateway-service-project.jar"]