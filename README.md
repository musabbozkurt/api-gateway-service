### Zuul API Gateway with Swagger2 Features
  ```
    * Zuul Api Gateway
    * Springfox Swagger2
  ```

## Versions

  ```
    * Spring Boot Version -> 2.6.4
    * Springfox Swagger2 Version -> 3.0.0
    * projectlombok Version -> 1.18.22
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
      
    * Service Endpoints Without API Gateway
    
      * http://localhost:8082/api/users
      * http://localhost:8082/api/user/role/ADMIN
      * http://localhost:8082/api/user/1
      
      * http://localhost:8081/students/
      * http://localhost:8081/students/role/ADMIN2
      * http://localhost:8081/students/1
  ```