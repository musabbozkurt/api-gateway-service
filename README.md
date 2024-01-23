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
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#References">References</a></li>
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
    * openai-service
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
    * HCaptcha repo migration is completed under the student-service
    * openai-service repo migration is completed
   ```

<!-- GETTING STARTED -->

## Getting Started

To get a local copy up and running please follow these simple steps.

### Prerequisites

Followings should be installed and links for how to install them.

* Java 17 or higher [How to install Java](https://java.com/en/download/help/download_options.html)
* Maven [How to install Maven](https://maven.apache.org/install.html)
* Lombok [How to install Lombok](https://www.baeldung.com/lombok-ide)
* Docker [How to install Docker](https://docs.docker.com/get-docker)

    ```
    * Java 17 or higher version should be installed on your machine -> export JAVA_HOME=$(/usr/libexec/java_home -v 17)
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
        * Fill the necessary fields. 
            * This url can guide you -> https://examples.javacodegeeks.com/wp-content/uploads/2020/12/springboot-google-captcha-google-config-img1.jpg 
        * Copy SITE KEY and SECRET KEY and add them into related fields that are in the api-gateway/src/main/resources/application.yml file
        * HCaptcha integration was implemented in 4 different ways in com/mb/studentservice/api/controller/HCaptchaController.java (OPTIONAL)
    
    * Postman can be installed (OPTIONAL) -> https://www.postman.com/downloads/
        * If Postman is installed, import files that are under the postman_collection folder
        * How to import postman collection -> https://learning.postman.com/docs/getting-started/importing-and-exporting-data/#importing-postman-data
  
    * Run the following command before running payment-service -> mvn clean install or mvn clean package
    * Run the following command in each service directory to run Spring Boot Applications -> mvn spring-boot:run
  
    * Create new secret key and replace all YOUR_API_KEY_HERE in code with this new key -> https://platform.openai.com/account/api-keys
  ```

### Installation

1. Clone the repo
   ```sh
    git clone https://github.com/musabbozkurt/api-gateway-service.git
   ```
2. Run Spring Boot applications

    1. `mvn clean install` or `mvn clean package`
    2. `mvn spring-boot:run`

3. Additional information to access endpoints, swagger and actuator

    * Swagger: http://localhost:8080/swagger-ui/ or http://localhost:8080/swagger-ui/index.html
    * Actuator: http://localhost:8080/actuator

4. How to run in Docker
   ```sh
    1 - Open Docker Quickstart Terminal
    2 - Go to the project directory from Docker Quickstart Terminal
    3 - Create Docker image by typing the following command -> docker build -t api-gateway-service-project.jar
    4 - Type the following command to make sure docker image has been created -> docker image ls
    5 - Type the following command to run docker image -> docker run -p 9090:8080 api-gateway-service-project.jar
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

* Keycloak integration with Spring Boot Project
    - https://www.keycloak.org/docs/latest/securing_apps/#_spring_boot_adapter
    - https://www.youtube.com/watch?v=rcvAmBoDlLk
    - Keycloak installation -> https://gruchalski.com/posts/2020-09-03-keycloak-with-docker-compose/
    - https://www.keycloak.org/docs/latest/server_admin/#_service_accounts
    - https://huongdanjava.com/get-access-token-using-the-grant-type-resource-owner-password-credentials-of-oauth-2-0-from-keycloak.html
    - https://developers.redhat.com/blog/2020/11/24/authentication-and-authorization-using-the-keycloak-rest-api#
    - https://www.baeldung.com/postman-keycloak-endpoints
    - https://stackoverflow.com/a/49127022
    - https://www.programcreek.com/java-api-examples/?api=org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
    - https://github.com/edwin/java-keycloak-integration

* https://www.baeldung.com/spring-cloud-feign-oauth-token
* https://www.codementor.io/@cristianrosu948/protecting-your-spring-boot-rest-endpoints-with-google-recaptcha-and-aop-pn7a88s7w
* https://www.baeldung.com/spring-security-registration-captcha
* https://examples.javacodegeeks.com/using-google-captcha-with-spring-boot-application/
* https://stackoverflow.com/a/44924353
* https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api

* HCaptcha integration with Spring Boot Project
    - [HCaptcha Developer Guide documentation](https://docs.hcaptcha.com/)
    - [HCaptcha Test Keys](https://docs.hcaptcha.com/#integration-testing-test-keys)
    - [HCaptcha Java Example]( https://golb.hplar.ch/2020/05/hcaptcha.html)

* OpenAI integration with Spring Boot Project
    - [Create new secret key and replace all YOUR_API_KEY_HERE in code with this new key](https://platform.openai.com/account/api-keys)
    - [openai-test-requests.http](openai-service%2Fopenai-test-requests.http)

* API Gateway with Spring Cloud Gateway in Java
    * [Building an API Gateway in Java with Spring Cloud Gateway](https://www.youtube.com/watch?v=EKoq98KqvrI)
    * [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/index.html)

