<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href="#summary">Summary</a>
      <ul>
        <li><a href="#services">Services</a></li>
        <li><a href="#features">Features</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#versions">Versions</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#References">References</a></li>
    <li><a href="#todo">TODO</a></li>
  </ol>
</details>


<!-- SUMMARY -->

## Summary

api-gateway-service project established by combination of the following services and features.

### Services

   ```
    * api-gateway
    * payment-service
    * student-service
    * swagger2-application
   ```

### Features

   ```
    * Zuul Api Gateway
    * Springfox Swagger2
    * Event Driven Architecture with RabbitMQ
    * Sleuth and Zipkin dependencies to track the logs
    * Postman collection to test by using Postman
    * Keycloak integration is completed under the payment-service
    * Feign Client secure call with Keycloak integration is completed under the student-service
    * MDC was added to improve logging between microservices
    * Google reCAPTCHA was added to secure endpoint calls
   ```

<!-- GETTING STARTED -->

## Getting Started

To get a local copy up and running please follow these simple steps.

### Prerequisites

Followings should be installed and links for how to install them.

* Java 11 or higher [How to install Java](https://java.com/en/download/help/download_options.html)
* Maven [How to install Maven](https://maven.apache.org/install.html)
* Lombok [How to install Lombok](https://www.baeldung.com/lombok-ide)
* Docker [How to install Docker](https://docs.docker.com/get-docker)

    ```
    * Java 11 or higher version should be installed on your machine
    * Install Apache Maven 
    
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
    
    **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** 
    **** If you want to ignore Google reCAPTCHA, remove @RequiresCaptcha from api-gateway/src/main/java/com/mb/apigateway/filter/PreFilter.java ****
    ****                               Don't need to do the steps under -> Google reCAPTCHA installation                                        ****
    **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** **** 
      
    * Google reCAPTCHA installation
        * Log in to the following url -> https://www.google.com/recaptcha/admin/create
        * Fill the necassary fields. 
            * This url can guide you -> https://examples.javacodegeeks.com/wp-content/uploads/2020/12/springboot-google-captcha-google-config-img1.jpg 
        * Copy SITE KEY and SECRET KEY and add them into related fields that are in the api-gateway/src/main/resources/application.yml file
    
    * Postman can be installed (Optional) -> https://www.postman.com/downloads/
        * If Postman is installed, import files that are under the postman_collection folder
        * How to import postman collection -> https://learning.postman.com/docs/getting-started/importing-and-exporting-data/#importing-postman-data
  
    * Run the following command before running payment-service -> clean install
    * Run the following command in each service directory to run Spring Boot Applications -> mvn spring-boot:run
  ```

### Installation

1. Clone the repo
   ```sh
    git clone https://github.com/musabbozkurt/api-gateway-service.git
   ```
2. Run Spring Boot applications
   ```sh
    mvn spring-boot:run
   ```
3. Additional information to access endpoints, swagger and actuator

   ```sh
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
      
      * http://localhost:8080/gateway/students/payments
   ```
4. How to run in Docker
   ```sh
    1 - Open Docker Quickstart Terminal
    2 - Go to the project directory from Docker Quickstart Terminal
    3 - Create Docker image by typing the following command -> docker build -t api-gateway-service-project.jar
    4 - Type the following command to make sure docker image has been created -> docker image ls
    5 - Type the following command to run docker image -> docker run -p 9090:8080 api-gateway-service-project.jar
   ```

<!-- VERSIONS -->

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

<!-- CONTRIBUTING -->

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/amazing-feature`)
3. Commit your Changes (`git commit -m 'Add some amazing features'`)
4. Push to the Branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

<!-- CONTACT -->

## Contact

* Musab Bozkurt - [Linkedin](https://tr.linkedin.com/in/musab-bozkurt-24924986)

* Project
  Link: [https://github.com/musabbozkurt/api-gateway-service.git](https://github.com/musabbozkurt/api-gateway-service.git)

<!-- REFERENCES -->

### References

  ```
    * Keycloak installation -> https://gruchalski.com/posts/2020-09-03-keycloak-with-docker-compose/
    * Keycloak integration with Spring Boot Project 
        -> https://www.keycloak.org/docs/latest/securing_apps/#_spring_boot_adapter
        -> https://www.youtube.com/watch?v=rcvAmBoDlLk
    * https://www.keycloak.org/docs/latest/server_admin/#_service_accounts
    * https://www.baeldung.com/spring-cloud-feign-oauth-token
    * https://huongdanjava.com/get-access-token-using-the-grant-type-resource-owner-password-credentials-of-oauth-2-0-from-keycloak.html
    * https://developers.redhat.com/blog/2020/11/24/authentication-and-authorization-using-the-keycloak-rest-api#
    * https://www.baeldung.com/postman-keycloak-endpoints
    * https://www.codementor.io/@cristianrosu948/protecting-your-spring-boot-rest-endpoints-with-google-recaptcha-and-aop-pn7a88s7w
    * https://www.baeldung.com/spring-security-registration-captcha
    * https://examples.javacodegeeks.com/using-google-captcha-with-spring-boot-application/
    * https://stackoverflow.com/a/44924353
  ```

<!-- TODO -->

### TODO

```
* Standardized API Exception Handling will be added. 
  * References
    * https://medium.com/@georgeberar/springboot-standardized-api-exception-handling-f31510861350
* webhook-service will be added. 
  * References
    * https://www.twilio.com/blog/codieren-von-twilio-webhooks-in-java-mit-spring-boot
    * https://dreamix.eu/blog/java/webhook-with-spring-boot-and-h2-database
* Distributed transactions in Spring, with and without XA and ChainedTransactionManager for rabbitMQ will be added. 
  * References
    * https://lifeinide.com/post/2017-12-29-spring-boot-rabbitmq-transactions/
    * https://stackoverflow.com/questions/46721195/transaction-handling-rabbit-mq-and-spring-amqp
    * https://www.infoworld.com/article/2077963/distributed-transactions-in-spring--with-and-without-xa.html
    * https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/transaction/ChainedTransactionManager.html
    * https://www.rabbitmq.com/api-guide.html
    * https://www.javainuse.com/camel/camel_bootsqltransact
    * https://mike-costello.github.io/2020/04/01/Using_Debezium_With_AMQP_Events/
    * https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/
    * https://dzone.com/articles/implementing-the-outbox-pattern
    * Outbox pattern with Debezium, PostgreSQL and RabbitMQ
* Elasticsearch, Kibana or Grafana with docker-compose will be added. 
  * References
    * 
```
