### Instructions

  ```
    * Java 11 or higher version should be installed on your machine
    * Install Apache Maven -> https://maven.apache.org/install.html 
    
    * Docker installation 
        * Please use the following link to install docker on your machine -> https://docs.docker.com/get-docker/
        * Run the following command command in the docker-compose.yml directory 
            to install RabbitMQ, PostgreSQL and Keycloak -> docker-compose up -d
        * After docker images are installed, run the following command on terminal or cmd 
          to create a Keycloak initial admin user.
          
           * docker exec local_keycloak \
                 /opt/jboss/keycloak/bin/add-user-keycloak.sh \
                 -u admin \
                 -p admin \
             && docker restart local_keycloak 
             
        * Log in to http://localhost:28080/auth/ with username: admin and password: admin
    
    * Postman can be installed (Optional) -> https://www.postman.com/downloads/
        * If Postman is installed, import api-gateway-service.postman_collection.json which is under postman_collection folder
        * How to import postman collection -> https://learning.postman.com/docs/getting-started/importing-and-exporting-data/#importing-postman-data
  
    * Run the following command before running payment-service -> clean install
    * Run the following command in each service directory to run Spring Boot Applications -> mvn spring-boot:run
  ```

### Zuul API Gateway with Swagger2 Features and Event Driven Architecture with RabbitMQ

  ```
    * Zuul Api Gateway
    * Springfox Swagger2
    * Event Driven Architecture with RabbitMQ
    * Sleuth and Zipkin dependencies were added
    * Postman collection was added
    * Keycloak integration is completed under the payment-service
  ```

## Versions

  ```
    * Spring Boot Version -> 2.6.4
    * Springfox Swagger2 Version -> 3.0.0
    * projectlombok Version -> 1.18.22
    * spring-cloud-starter-stream-rabbit -> parent project version -> 2.6.4
    * spring-cloud-starter-sleuth -> parent project version -> 2.6.4
    * spring-cloud-sleuth-zipkin -> parent project version -> 2.6.4
    * spring-cloud Version -> 2021.0.1
    * org.mapstruct Version -> 1.4.2.Final 
    * keycloak-spring-boot-starter Version -> 16.1.1 
  ```

### Additional information to access endpoints, swagger and actuator

  ```
    * Swagger Url
      * swagger2-application endpoints
      
          * http://localhost:8080/gateway/api/v2/api-docs
          * http://localhost:8082/swagger-ui/index.html
      
      * student-service endpoints
      
          * http://localhost:8080/gateway/students/v2/api-docs
          * http://localhost:8081/swagger-ui/index.html
    
    * Actuator Url
    
      * http://localhost:8082/actuator
      
    * API Gateway Endpoints 
    
      * http://localhost:8080/gateway/api/users
      * http://localhost:8080/gateway/api/user/role/ADMIN
      * http://localhost:8080/gateway/api/user/1
      
      * http://localhost:8080/gateway/students/
      * http://localhost:8080/gateway/students/role/ADMIN2
      * http://localhost:8080/gateway/students/1
      
      * GET http://localhost:8080/gateway/payments/
      * GET http://localhost:8080/gateway/payments/1
      * POST http://localhost:8080/gateway/payments
      
    * Service Endpoints Without API Gateway
    
      * http://localhost:8082/api/users
      * http://localhost:8082/api/user/role/ADMIN
      * http://localhost:8082/api/user/1
      
      * http://localhost:8082/api/events TO PUBLISH AN EVENT
      
      * http://localhost:8081/students/
      * http://localhost:8081/students/role/ADMIN2
      * http://localhost:8081/students/1
      
      * http://localhost:8081/students/events TO PUBLISH AN EVENT
      
      * GET http://localhost:8083/payments/
      * GET http://localhost:8083/payments/1
      * POST http://localhost:8083/payments
  ```

### TODO

  ```
    * webhook-service will be added. 
      * References
        * https://www.twilio.com/blog/codieren-von-twilio-webhooks-in-java-mit-spring-boot
        * https://dreamix.eu/blog/java/webhook-with-spring-boot-and-h2-database
  ```

### References

  ```
    * Keycloak installation -> https://gruchalski.com/posts/2020-09-03-keycloak-with-docker-compose/
    * Keycloak integration with Spring Boot Project 
        -> https://www.keycloak.org/docs/latest/securing_apps/#_spring_boot_adapter
        -> https://www.youtube.com/watch?v=rcvAmBoDlLk
  ```
